package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.ChatRequest;
import com.ahmedyassin.TravelSmart.dto.ChatResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChatbotService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final ChatbotDataService dataService;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:openai/gpt-3.5-turbo}")
    private String model;

    private static final String SYSTEM_PROMPT = """
            You are TravelSmart AI Assistant, an intelligent assistant for travel agency administrators with REAL-TIME DATABASE ACCESS.

            You have access to the following database information that is provided to you in each message:
            - Flight data (total count, active/inactive, airlines, destinations, prices)
            - Hotel data (total count, cities, star ratings, rooms, prices)
            - Reservation data (pending, confirmed, cancelled, revenue)
            - Circuit/Tour data
            - Client/Customer data
            -Activity

            IMPORTANT INSTRUCTIONS:
            1. When users ask about data (flights, hotels, reservations, etc.), USE THE DATABASE CONTEXT PROVIDED to give accurate answers
            2. The database context is real-time data from the system - trust and use it
            3. Be specific with numbers and statistics from the context
            4. If the context shows "0" items, tell the user there are no items in that category
            5. Format responses nicely with emojis and markdown for better readability

            You help administrators with:
            - 📊 **Dashboard & Statistics**: Providing real-time counts and analytics
            - ✈️ **Flight Management**: Viewing flight counts, searching flights, status updates
            - 🏨 **Hotel Management**: Hotel inventory, room counts, city distribution
            - 📋 **Booking Management**: Reservation status, pending approvals, revenue tracking
            - 🗺️ **Circuit Management**: Tour packages and itineraries
            - 👥 **Customer Management**: Client statistics and information

            Always provide helpful, accurate information based on the database context provided.
            When asked "how many", "count", "total", "list" - ALWAYS refer to the DATABASE CONTEXT section.
            """;

    public ChatbotService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
            ChatbotDataService dataService) {
        this.webClient = webClientBuilder
                .baseUrl("https://openrouter.ai/api/v1")
                .build();
        this.objectMapper = objectMapper;
        this.dataService = dataService;
    }

    public Mono<ChatResponse> chat(ChatRequest request) {
        try {
            // Analyze the user's message to determine what data they need
            String userMessage = request.getMessage().toLowerCase();
            String databaseContext = buildRelevantContext(userMessage);

            List<Map<String, String>> messages = buildMessages(request, databaseContext);
            Map<String, Object> requestBody = buildRequestBody(messages);

            return webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("HTTP-Referer", "https://travelsmart.com")
                    .header("X-Title", "TravelSmart Admin Assistant")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::parseResponse)
                    .onErrorResume(e -> {
                        log.error("Error calling OpenRouter API: {}", e.getMessage());
                        return Mono.just(ChatResponse.error("Sorry, I'm having trouble connecting. Please try again."));
                    });
        } catch (Exception e) {
            log.error("Error preparing chat request: {}", e.getMessage());
            return Mono.just(ChatResponse.error("An error occurred. Please try again."));
        }
    }

    /**
     * Build relevant database context based on user's question
     */
    private String buildRelevantContext(String userMessage) {
        StringBuilder context = new StringBuilder();
        context.append("\n\n=== DATABASE CONTEXT (REAL-TIME DATA) ===\n\n");

        // Always include basic stats
        context.append(dataService.buildDatabaseContext());
        context.append("\n");

        // Add detailed data based on what the user is asking about
        if (containsAny(userMessage, "flight", "flights", "plane", "airline", "fly", "aviation")) {
            context.append("\n--- DETAILED FLIGHT DATA ---\n");
            context.append(dataService.getFlightsSummary());
            context.append("\n");
        }

        if (containsAny(userMessage, "hotel", "hotels", "room", "rooms", "accommodation", "stay", "lodging")) {
            context.append("\n--- DETAILED HOTEL DATA ---\n");
            context.append(dataService.getHotelsSummary());
            context.append("\n");
        }

        if (containsAny(userMessage, "reservation", "booking", "booked", "reservations", "bookings", "order",
                "orders")) {
            context.append("\n--- DETAILED RESERVATION DATA ---\n");
            context.append(dataService.getReservationsSummary());
            context.append("\n");
        }

        if (containsAny(userMessage, "pending", "waiting", "approval", "attention", "urgent")) {
            context.append("\n--- PENDING ITEMS ---\n");
            context.append(dataService.getPendingReservations());
            context.append("\n");
        }

        if (containsAny(userMessage, "circuit", "tour", "package", "trip", "itinerary")) {
            context.append("\n--- CIRCUIT/TOUR DATA ---\n");
            context.append(dataService.getCircuitsSummary());
            context.append("\n");
        }

        if (containsAny(userMessage, "client", "customer", "user", "member", "registered")) {
            context.append("\n--- CLIENT DATA ---\n");
            context.append(dataService.getClientsSummary());
            context.append("\n");
        }

        if (containsAny(userMessage, "list", "show", "display", "all")) {
            if (containsAny(userMessage, "flight")) {
                context.append("\n--- FLIGHT LIST ---\n");
                context.append(dataService.getFlightsList(10));
            }
            if (containsAny(userMessage, "hotel")) {
                context.append("\n--- HOTEL LIST ---\n");
                context.append(dataService.getHotelsList(10));
            }
            if (containsAny(userMessage, "reservation", "booking")) {
                context.append("\n--- RECENT RESERVATIONS ---\n");
                context.append(dataService.getRecentReservations(10));
            }
        }

        if (containsAny(userMessage, "recent", "latest", "new", "today", "week")) {
            context.append("\n--- RECENT RESERVATIONS ---\n");
            context.append(dataService.getRecentReservations(5));
            context.append("\n");
        }

        // Handle search queries
        if (containsAny(userMessage, "search", "find", "from", "to", "in")) {
            // Try to extract search parameters
            if (containsAny(userMessage, "flight")) {
                String origin = extractCity(userMessage, "from");
                String destination = extractCity(userMessage, "to");
                if (origin != null || destination != null) {
                    context.append("\n--- SEARCH RESULTS ---\n");
                    context.append(dataService.searchFlights(origin, destination));
                }
            }
            if (containsAny(userMessage, "hotel")) {
                String city = extractCity(userMessage, "in");
                if (city != null) {
                    context.append("\n--- SEARCH RESULTS ---\n");
                    context.append(dataService.searchHotels(city, null));
                }
            }
        }

        context.append("\n=== END DATABASE CONTEXT ===\n");

        return context.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String extractCity(String text, String preposition) {
        // Simple extraction: look for word after preposition
        Pattern pattern = Pattern.compile(preposition + "\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private List<Map<String, String>> buildMessages(ChatRequest request, String databaseContext) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Add system prompt
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        // Add conversation history (limit to last 10 messages for context)
        if (request.getHistory() != null) {
            int startIndex = Math.max(0, request.getHistory().size() - 10);
            for (int i = startIndex; i < request.getHistory().size(); i++) {
                ChatRequest.ChatMessageDto msg = request.getHistory().get(i);
                Map<String, String> historyMessage = new HashMap<>();
                historyMessage.put("role", msg.getRole());
                historyMessage.put("content", msg.getContent());
                messages.add(historyMessage);
            }
        }

        // Add current user message WITH database context
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", request.getMessage() + databaseContext);
        messages.add(userMessage);

        return messages;
    }

    private Map<String, Object> buildRequestBody(List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1500);
        requestBody.put("temperature", 0.7);
        return requestBody;
    }

    private ChatResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Check for error response
            if (root.has("error")) {
                String errorMessage = root.get("error").has("message")
                        ? root.get("error").get("message").asText()
                        : "Unknown error occurred";
                log.error("OpenRouter API error: {}", errorMessage);
                return ChatResponse.error("Sorry, I encountered an issue. Please try again.");
            }

            // Parse successful response
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    String content = message.get("content").asText();
                    return ChatResponse.success(content);
                }
            }

            log.warn("Unexpected response format from OpenRouter: {}", responseBody);
            return ChatResponse.error("Received an unexpected response. Please try again.");
        } catch (Exception e) {
            log.error("Error parsing OpenRouter response: {}", e.getMessage());
            return ChatResponse.error("Error processing response. Please try again.");
        }
    }
}
