package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.TravelRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    private final WebClient webClient;
    private final String apiKey;
    private final String geminiApiUrl;
    private final ObjectMapper objectMapper;

    public GeminiAIService(WebClient.Builder webClientBuilder,
                           @Value("${gemini.api.key}") String apiKey,
                           @Value("${gemini.api.base-url}") String geminiApiUrl,
                           ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.apiKey = apiKey;
        this.geminiApiUrl = geminiApiUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Appelle l'API Gemini pour générer un plan de voyage basé sur une TravelRequest.
     * Retourne le JSON du plan généré sous forme de String.
     */
    public Mono<String> generateTravelPlan(TravelRequest travelRequest) {
        String prompt = buildPrompt(travelRequest);


        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsArray = requestBody.putArray("contents"); // "contents": []

        ObjectNode contentObject = contentsArray.addObject(); // "contents": [{}]
        ArrayNode partsArray = contentObject.putArray("parts"); // "contents": [{"parts": []}]

        ObjectNode textObject = partsArray.addObject(); // "contents": [{"parts": [{}]}]
        textObject.put("text", prompt); // "contents": [{"parts": [{"text": "Your prompt"}]}]



        System.out.println("Envoi du prompt à Gemini : " + prompt);
        System.out.println("Full JSON request body sent to Gemini: " + requestBody.toString());



        System.out.println("DEBUGGING API CALL:");
        System.out.println("  Gemini API URL (base): " + this.geminiApiUrl);
        System.out.println("  API Key used: " + this.apiKey); // Be careful with this in production logs
        System.out.println("  Full URI being hit: " + (this.geminiApiUrl + "?key=" + this.apiKey));
        System.out.println("  Request Body (as sent): " + requestBody.toString());
        return webClient.post()
                .uri(geminiApiUrl + "?key=" + apiKey) // Assuming geminiApiUrl already contains "?key=" if needed, or append it here
                .header("Content-Type", "application/json")
                .bodyValue(requestBody.toString())
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .map(responseNode -> {
                    try {
                        String aiResponseContent = responseNode
                                .path("candidates")
                                .path(0)
                                .path("content")
                                .path("parts")
                                .path(0)
                                .path("text")
                                .asText();
                        System.out.println("Réponse brute de Gemini : " + aiResponseContent);

                        String cleanedJson = cleanGeminiJsonResponse(aiResponseContent);

                        // Validate if the cleanedJson is valid JSON.
                        // If not, it means Gemini didn't return valid JSON,
                        // and you might want to log this or throw a specific exception.
                        objectMapper.readTree(cleanedJson); // Throws JsonProcessingException if invalid

                        return cleanedJson;

                    } catch (JsonProcessingException e) {
                        System.err.println("Erreur lors du parsing de la réponse JSON de Gemini (JSON invalide): " + e.getMessage());
                        // Consider logging the full aiResponseContent here for debugging malformed responses from Gemini
                        throw new RuntimeException("Failed to parse Gemini's JSON response due to invalid format.", e);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du parsing de la réponse Gemini: " + e.getMessage());
                        throw new RuntimeException("Failed to parse Gemini response or extract text", e);
                    }
                })
                .doOnError(e -> System.err.println("Erreur lors de l'appel à l'API Gemini: " + e.getMessage()));
    }

    // Méthode pour construire le prompt détaillé
    private String buildPrompt(TravelRequest request) {
        System.out.println(request.getPays());
        StringBuilder prompt = new StringBuilder();
        prompt.append("En tant qu'expert en planification de voyages pour une entreprise, génère un plan de voyage détaillé. ");
        prompt.append("L'objectif est un voyage professionnel pour l'équipe.");
        prompt.append(String.format("Le voyage est prévu du %s au %s, pour %d personne(s). ",
                request.getStartDate(), request.getEndDate(), request.getTravelers().size()));
        prompt.append(String.format("- Destination : %s\n", request.getPays())); // Validated country
        prompt.append(String.format("Il s'agit d'un voyage pour l'équipe '%s' (ID: %s), créé par '%s' (ID: %s). ",
                request.getTeam().getName(), request.getTeam().getId(),
                request.getCreator().getUsername(), request.getCreator().getId()));

        // Liste des voyageurs
        String travelerNames = request.getTravelers().stream()
                .map(t -> t.getUser().getUsername() + " (ID: " + t.getUser().getId() + ")")
                .collect(Collectors.joining(", "));
        prompt.append(String.format("Les voyageurs inclus sont : %s. ", travelerNames));

        prompt.append("Propose le 'meilleur' plan possible en termes d'efficacité pour un voyage d'affaires. ");

        // TRÈS IMPORTANT : Demandez le format JSON explicitement pour votre DTO simple
        prompt.append("Structure la réponse **uniquement** en JSON, en utilisant la structure suivante pour le plan unique : ");
        prompt.append("```json\n"); // Ceci aide Gemini à comprendre qu'il doit générer du JSON pur
        prompt.append("{\n");
        prompt.append("  \"planName\": \"[Nom du plan, ex: Plan Affaires Standard]\",\n");
        prompt.append("  \"estimatedTotalCost\": [Coût total estimé en dinars, ex: 1500.75],\n");
        prompt.append("  \"flightsInfo\": \"[Détails des vols: compagnies, horaires, aéroports]\",\n");
        prompt.append("  \"accommodationInfo\": \"[Détails de l'hébergement: nom de l'hôtel, nombre d'étoiles, emplacement]\",\n");
        prompt.append("  \"localTransportInfo\": \"[Détails du transport local: options, comment se déplacer]\",\n");
        prompt.append("  \"activitiesInfo\": \"[Suggestions d'activités/agenda pertinent pour un voyage d'affaires]\",\n");
        prompt.append("  \"visaRequirementsInfo\": \"[Exigences de visa et documents si applicables]\",\n");
        prompt.append("  \"insuranceOptionsInfo\": \"[Suggestions d'options d'assurance voyage]\",\n");
        prompt.append("  \"costBreakdown\": \"[Répartition détaillée des coûts (vols, hôtel, etc.) sous forme textuelle ou JSON string si vous préférez]\"\n");
        prompt.append("}\n");
        prompt.append("```\n");
        prompt.append("Si une information n'est pas applicable ou ne peut être déterminée, utilise 'N/A' pour sa valeur. Ne pas inclure de texte explicatif avant ou après le bloc JSON.");
        prompt.append("vous navez pas aucune contrainte de budget , donner les meilleur hotel avec les nom et tous, et plan de voyage au repere qualité prix , cad pas les plus cher et pas les plus diminué");
        prompt.append(", aussi les cous sont en dinars , ajouter Dt aux chiffre, et la nationalité est tunisien est tunisenne, structurer le travail demandé, le travail nest pas lisible");



        return prompt.toString();
    }

    // Méthode utilitaire pour nettoyer la réponse de Gemini
    private String cleanGeminiJsonResponse(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + 1);
        }

        response = response.replace("```json", "").replace("```", "").trim();
        return response;
    }
}