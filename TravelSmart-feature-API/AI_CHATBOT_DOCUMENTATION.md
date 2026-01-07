# TravelSmart AI Chatbot Documentation

## Table of Contents

1. [Introduction](#introduction)
2. [System Architecture](#system-architecture)
3. [Technologies Used](#technologies-used)
4. [Backend Implementation](#backend-implementation)
5. [Frontend Implementation](#frontend-implementation)
6. [Database Integration](#database-integration)
7. [API Endpoints](#api-endpoints)
8. [How It Works](#how-it-works)
9. [Configuration](#configuration)
10. [Security Considerations](#security-considerations)
11. [Usage Examples](#usage-examples)
12. [Future Improvements](#future-improvements)

---

## Introduction

### Overview

The TravelSmart AI Chatbot is an intelligent assistant designed to help travel agency administrators manage their operations efficiently. Unlike traditional chatbots that provide static responses, this chatbot is powered by **OpenAI's GPT model** and has **real-time database access**, enabling it to provide accurate, data-driven responses about flights, hotels, reservations, and more.

### Purpose

The chatbot serves as a virtual assistant for administrators, capable of:

- Providing real-time statistics and analytics
- Querying the database for specific information
- Answering questions about flights, hotels, and reservations
- Assisting with operational tasks and decision-making
- Offering guidance on system navigation and features

### Key Features

| Feature           | Description                                       |
| ----------------- | ------------------------------------------------- |
| 🤖 AI-Powered     | Uses OpenAI GPT model via OpenRouter API          |
| 📊 Real-time Data | Direct access to PostgreSQL database              |
| 💬 Conversational | Maintains context across multiple messages        |
| 🔍 Smart Search   | Can search flights and hotels by various criteria |
| 📈 Analytics      | Provides statistics and summaries on demand       |
| 🎨 Modern UI      | Professional floating chat interface              |

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Angular)                          │
│  ┌─────────────────┐    ┌─────────────────┐    ┌────────────────┐  │
│  │ ChatbotComponent│───▶│ ChatbotService  │───▶│  HTTP Client   │  │
│  │     (UI)        │    │   (Angular)     │    │                │  │
│  └─────────────────┘    └─────────────────┘    └───────┬────────┘  │
└───────────────────────────────────────────────────────┼────────────┘
                                                        │
                                                        ▼ HTTP POST
┌───────────────────────────────────────────────────────┼────────────┐
│                      BACKEND (Spring Boot)            │            │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────┴──────────┐ │
│  │ChatbotController│───▶│ ChatbotService  │───▶│ChatbotDataSvc  │ │
│  │  (REST API)     │    │  (AI Logic)     │    │(DB Queries)    │ │
│  └─────────────────┘    └────────┬────────┘    └───────┬────────┘ │
│                                  │                      │          │
│                                  ▼                      ▼          │
│                         ┌────────────────┐    ┌────────────────┐  │
│                         │ OpenRouter API │    │  Repositories  │  │
│                         │   (GPT Model)  │    │  (JPA/DB)      │  │
│                         └────────────────┘    └───────┬────────┘  │
└───────────────────────────────────────────────────────┼────────────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │   PostgreSQL   │
                                               │   Database     │
                                               └────────────────┘
```

### Component Interaction Flow

```
User Message ──▶ Angular Component ──▶ Angular Service ──▶ Spring Controller
                                                                   │
                                                                   ▼
AI Response ◀── Angular Component ◀── Angular Service ◀── Spring Service
                                                                   │
                                                    ┌──────────────┴──────────────┐
                                                    │                             │
                                                    ▼                             ▼
                                            ChatbotDataService            OpenRouter API
                                            (Database Context)            (AI Processing)
```

---

## Technologies Used

### Backend Technologies

| Technology      | Version | Purpose                                  |
| --------------- | ------- | ---------------------------------------- |
| Java            | 21      | Programming language                     |
| Spring Boot     | 3.4.7   | Application framework                    |
| Spring WebFlux  | 3.4.7   | Reactive HTTP client for API calls       |
| Spring Data JPA | 3.4.7   | Database access layer                    |
| PostgreSQL      | Latest  | Database                                 |
| Lombok          | 1.18.30 | Code generation (getters, setters, etc.) |
| Jackson         | 2.x     | JSON processing                          |

### Frontend Technologies

| Technology         | Version | Purpose              |
| ------------------ | ------- | -------------------- |
| Angular            | 17+     | Frontend framework   |
| TypeScript         | 5.x     | Programming language |
| RxJS               | 7.x     | Reactive programming |
| Angular Animations | 17+     | UI animations        |

### External APIs

| API            | Provider      | Purpose                          |
| -------------- | ------------- | -------------------------------- |
| OpenRouter API | OpenRouter.ai | AI model gateway (GPT-3.5-turbo) |

---

## Backend Implementation

### Project Structure

```
src/main/java/com/ahmedyassin/TravelSmart/
├── controllers/
│   └── ChatbotController.java      # REST API endpoint
├── dto/
│   ├── ChatRequest.java            # Request DTO
│   └── ChatResponse.java           # Response DTO
├── services/
│   ├── ChatbotService.java         # AI integration logic
│   └── ChatbotDataService.java     # Database queries
└── Security/
    └── SecurityConfig.java         # Security configuration
```

### 1. Data Transfer Objects (DTOs)

#### ChatRequest.java

```java
package com.ahmedyassin.TravelSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;                    // Current user message
    private List<ChatMessageDto> history;      // Conversation history

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageDto {
        private String role;      // "user" or "assistant"
        private String content;   // Message content
    }
}
```

**Purpose**: Encapsulates the incoming chat request containing:

- The current user message
- The conversation history for context awareness

#### ChatResponse.java

```java
package com.ahmedyassin.TravelSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;    // AI response message
    private boolean success;   // Success indicator
    private String error;      // Error message if failed

    public static ChatResponse success(String message) {
        return new ChatResponse(message, true, null);
    }

    public static ChatResponse error(String error) {
        return new ChatResponse(null, false, error);
    }
}
```

**Purpose**: Standardized response format with success/error handling.

### 2. ChatbotController.java

```java
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public Mono<ResponseEntity<ChatResponse>> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request: {}", request.getMessage());

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(ChatResponse.error("Message cannot be empty")));
        }

        return chatbotService.chat(request)
                .map(response -> {
                    if (response.isSuccess()) {
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.internalServerError().body(response);
                    }
                });
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
```

**Key Features**:

- Uses `Mono<>` for reactive/async processing
- Input validation for empty messages
- Health check endpoint for monitoring
- CORS enabled for frontend access

### 3. ChatbotService.java

This is the core service that:

1. Analyzes user messages to determine data needs
2. Fetches relevant database context
3. Sends requests to OpenRouter API
4. Parses and returns AI responses

**Key Methods**:

```java
public Mono<ChatResponse> chat(ChatRequest request) {
    // 1. Analyze user message
    String userMessage = request.getMessage().toLowerCase();

    // 2. Build relevant database context
    String databaseContext = buildRelevantContext(userMessage);

    // 3. Prepare messages with context
    List<Map<String, String>> messages = buildMessages(request, databaseContext);

    // 4. Call OpenRouter API
    return webClient.post()
            .uri("/chat/completions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .map(this::parseResponse);
}
```

**Context Building Logic**:

```java
private String buildRelevantContext(String userMessage) {
    StringBuilder context = new StringBuilder();

    // Always include basic stats
    context.append(dataService.buildDatabaseContext());

    // Add specific data based on keywords
    if (containsAny(userMessage, "flight", "flights", "airline")) {
        context.append(dataService.getFlightsSummary());
    }

    if (containsAny(userMessage, "hotel", "hotels", "room")) {
        context.append(dataService.getHotelsSummary());
    }

    if (containsAny(userMessage, "reservation", "booking")) {
        context.append(dataService.getReservationsSummary());
    }

    // ... more context based on keywords
    return context.toString();
}
```

**System Prompt**:

```java
private static final String SYSTEM_PROMPT = """
    You are TravelSmart AI Assistant, an intelligent assistant for travel
    agency administrators with REAL-TIME DATABASE ACCESS.

    You have access to the following database information:
    - Flight data (count, airlines, destinations, prices)
    - Hotel data (count, cities, star ratings, rooms, prices)
    - Reservation data (pending, confirmed, cancelled, revenue)
    - Circuit/Tour data
    - Client/Customer data

    IMPORTANT: Use the DATABASE CONTEXT PROVIDED to give accurate answers.
    Format responses with emojis and markdown for readability.
    """;
```

### 4. ChatbotDataService.java

This service provides database access methods:

| Method                         | Returns                                      |
| ------------------------------ | -------------------------------------------- |
| `getDashboardStats()`          | Overall statistics map                       |
| `getFlightsSummary()`          | Flight count, airlines, destinations, prices |
| `getHotelsSummary()`           | Hotel count, cities, star ratings, rooms     |
| `getReservationsSummary()`     | Reservation stats by status, revenue         |
| `getFlightsList(limit)`        | List of flights with details                 |
| `getHotelsList(limit)`         | List of hotels with details                  |
| `searchFlights(origin, dest)`  | Search flights by route                      |
| `searchHotels(city, stars)`    | Search hotels by criteria                    |
| `getPendingReservations()`     | Pending reservations list                    |
| `getRecentReservations(limit)` | Recent reservation activity                  |
| `getCircuitsSummary()`         | Tour/circuit statistics                      |
| `getClientsSummary()`          | Client statistics                            |
| `buildDatabaseContext()`       | Complete context string                      |

**Example Method - Flight Summary**:

```java
public String getFlightsSummary() {
    List<Flight> flights = flightRepository.findAll();
    long totalFlights = flights.size();
    long activeFlights = flights.stream()
        .filter(f -> Boolean.TRUE.equals(f.getIsActive()))
        .count();

    // Group by airline
    Map<String, Long> byAirline = flights.stream()
        .collect(Collectors.groupingBy(Flight::getAirline, Collectors.counting()));

    // Calculate price statistics
    DoubleSummaryStatistics priceStats = flights.stream()
        .mapToDouble(Flight::getPrice)
        .summaryStatistics();

    // Format response
    StringBuilder sb = new StringBuilder();
    sb.append("📊 **Flight Statistics:**\n");
    sb.append(String.format("- Total flights: **%d**\n", totalFlights));
    sb.append(String.format("- Active flights: **%d**\n", activeFlights));
    // ... more formatting

    return sb.toString();
}
```

---

## Frontend Implementation

### Project Structure

```
src/app/
├── services/
│   └── chatbot.service.ts          # API communication
├── shared/components/chatbot/
│   ├── chatbot.component.ts        # Component logic
│   ├── chatbot.component.html      # Template
│   └── chatbot.component.css       # Styles
└── app.component.html              # Main app (includes chatbot)
```

### 1. ChatbotService (Angular)

```typescript
@Injectable({
  providedIn: "root",
})
export class ChatbotService {
  private readonly apiUrl = "http://localhost:8085/api/chatbot";

  constructor(private http: HttpClient) {}

  sendMessage(
    message: string,
    history: ChatMessage[]
  ): Observable<ChatResponse> {
    const request: ChatRequest = { message, history };
    return this.http.post<ChatResponse>(`${this.apiUrl}/chat`, request).pipe(
      catchError((error) => {
        return of({
          message: "",
          success: false,
          error: "Unable to connect to the assistant.",
        });
      })
    );
  }

  checkHealth(): Observable<boolean> {
    return this.http
      .get(`${this.apiUrl}/health`, { responseType: "text" })
      .pipe(
        map(() => true),
        catchError(() => of(false))
      );
  }
}
```

### 2. ChatbotComponent

**Key Features**:

- Floating button with unread notification badge
- Slide-in/out animations
- Typing indicator while waiting for response
- Quick reply buttons for common actions
- Conversation history management
- Online/offline status indicator
- Clear chat functionality
- Responsive design with mobile support
- Dark mode support

**Component Logic Highlights**:

```typescript
export class ChatbotComponent implements OnInit, OnDestroy {
  isOpen = false;
  messages: ChatMessage[] = [];
  isTyping = false;
  conversationHistory: ApiChatMessage[] = [];

  sendMessage(): void {
    // 1. Add user message to UI
    this.messages.push(userMessage);

    // 2. Add to conversation history
    this.conversationHistory.push({ role: "user", content });

    // 3. Show typing indicator
    this.isTyping = true;

    // 4. Call API
    this.chatbotService
      .sendMessage(content, this.conversationHistory)
      .subscribe((response) => {
        this.isTyping = false;
        if (response.success) {
          // Add bot response
          this.conversationHistory.push({
            role: "assistant",
            content: response.message,
          });
          this.addBotMessage(response.message);
        }
      });
  }
}
```

### 3. UI Design

The chatbot features a modern, professional design:

- **Floating Button**: Fixed position bottom-right with pulse animation
- **Chat Window**: 380px wide, rounded corners, shadow effects
- **Header**: Gradient blue background with bot avatar and status
- **Messages**: Distinct styling for user (blue) and bot (white) messages
- **Input Area**: Clean input field with send button
- **Animations**: Smooth open/close, message fade-in effects

---

## Database Integration

### Repositories Used

| Repository            | Entity      | Data Provided                   |
| --------------------- | ----------- | ------------------------------- |
| FlightRepository      | Flight      | Flight listings, routes, prices |
| HotelRepository       | Hotel       | Hotel listings, rooms, ratings  |
| ReservationRepository | Reservation | Bookings, status, revenue       |
| RoomRepository        | Room        | Room availability               |
| CircuitRepository     | Circuit     | Tour packages                   |
| ClientRepository      | Client      | Customer data                   |

### Data Flow

```
User asks: "How many flights do we have?"
                    │
                    ▼
    ChatbotService detects "flight" keyword
                    │
                    ▼
    ChatbotDataService.getFlightsSummary() called
                    │
                    ▼
    FlightRepository.findAll() executed
                    │
                    ▼
    Data processed and formatted
                    │
                    ▼
    Context sent to AI with user message
                    │
                    ▼
    AI generates natural language response
                    │
                    ▼
    Response returned to user
```

---

## API Endpoints

### POST /api/chatbot/chat

Send a message to the chatbot.

**Request Body**:

```json
{
  "message": "How many flights do we have?",
  "history": [
    {
      "role": "user",
      "content": "Hello"
    },
    {
      "role": "assistant",
      "content": "Hello! How can I help you today?"
    }
  ]
}
```

**Success Response** (200 OK):

```json
{
  "message": "📊 **Flight Statistics:**\n- Total flights: **25**\n- Active flights: **20**\n...",
  "success": true,
  "error": null
}
```

**Error Response** (500 Internal Server Error):

```json
{
  "message": null,
  "success": false,
  "error": "Sorry, I encountered an issue. Please try again."
}
```

### GET /api/chatbot/health

Health check endpoint.

**Response** (200 OK):

```
Chatbot service is running
```

---

## How It Works

### Step-by-Step Process

1. **User Input**: User types a message in the chat interface
2. **Frontend Processing**: Angular component captures the message and conversation history
3. **API Request**: HTTP POST sent to `/api/chatbot/chat`
4. **Message Analysis**: Backend analyzes message for relevant keywords
5. **Database Query**: Relevant data fetched from PostgreSQL via JPA repositories
6. **Context Building**: Database results formatted into context string
7. **AI Request**: Message + context sent to OpenRouter API (GPT model)
8. **AI Processing**: GPT generates natural language response
9. **Response Parsing**: Backend parses AI response
10. **Frontend Display**: Response shown in chat interface with formatting

### Context Injection Example

**User Message**: "How many hotels do we have in Paris?"

**Injected Context**:

```
=== DATABASE CONTEXT (REAL-TIME DATA) ===

FLIGHTS: 25 total (20 active)
HOTELS: 15 total (12 active), 45 rooms
RESERVATIONS: 150 total (Pending: 10, Confirmed: 120, Cancelled: 20)

--- DETAILED HOTEL DATA ---
📊 **Hotel Statistics:**
- Total hotels: **15**
- Active hotels: **12**
- Total rooms: **45**

🏙️ **Hotels by City:**
- Paris: 5 hotels
- Marrakech: 4 hotels
- Dubai: 3 hotels

--- SEARCH RESULTS ---
🔍 Found **5 hotels** in 'Paris':
• **Hotel Le Marais** ⭐⭐⭐⭐ | Paris | $150.00/night | Active
• **Paris Grand Hotel** ⭐⭐⭐⭐⭐ | Paris | $280.00/night | Active
...

=== END DATABASE CONTEXT ===
```

**AI Response**: "Based on our current database, we have **5 hotels** in Paris! Here's a quick overview..."

---

## Configuration

### application.properties

```properties
# OpenRouter AI Configuration
openai.api.key=${OPENAI_API_KEY:your-api-key-here}
openai.api.model=${OPENAI_MODEL:openai/gpt-3.5-turbo}
```

### Environment Variables

| Variable         | Description        | Default              |
| ---------------- | ------------------ | -------------------- |
| `OPENAI_API_KEY` | OpenRouter API key | Required             |
| `OPENAI_MODEL`   | AI model to use    | openai/gpt-3.5-turbo |

### Security Configuration

The chatbot endpoint is publicly accessible (no authentication required):

```java
.requestMatchers("/api/chatbot/**").permitAll()
```

---

## Security Considerations

### Current Implementation

- API key stored in application.properties (should use environment variables in production)
- CORS enabled for frontend access
- Input validation on message content
- Error messages don't expose sensitive information

### Recommended Production Improvements

1. **Environment Variables**: Store API key in environment variables, not in code
2. **Rate Limiting**: Implement rate limiting to prevent abuse
3. **Authentication**: Require user authentication for chatbot access
4. **Input Sanitization**: Enhanced input validation and sanitization
5. **Logging**: Audit logging for compliance
6. **HTTPS**: Ensure all communication is encrypted

---

## Usage Examples

### Example Conversations

**1. Getting Flight Statistics**

```
User: How many flights do we have?

Bot: 📊 **Flight Statistics:**
- Total flights: **25**
- Active flights: **20**
- Inactive flights: **5**

✈️ **Flights by Airline:**
- Royal Air Maroc: 8 flights
- Air France: 6 flights
- Emirates: 5 flights

💰 **Price Range:** $150.00 - $1,200.00 (Avg: $450.00)
```

**2. Checking Pending Reservations**

```
User: Are there any pending reservations?

Bot: ⚠️ **Pending Reservations** (10 total):

• **FLIGHT** - RAM 205 Paris-Casablanca | client@email.com | $350.00 | Jan 05, 2026
• **HOTEL** - Marrakech Palace | guest@email.com | $420.00 | Jan 06, 2026
...

These reservations need your attention for confirmation.
```

**3. Searching Hotels**

```
User: Find hotels in Marrakech with 4 stars

Bot: 🔍 Found **3 hotels** in 'Marrakech' with 4+ stars:

• **La Mamounia** ⭐⭐⭐⭐⭐ | Marrakech | $350.00/night | Active
• **Royal Mansour** ⭐⭐⭐⭐⭐ | Marrakech | $500.00/night | Active
• **Riad Kniza** ⭐⭐⭐⭐ | Marrakech | $180.00/night | Active
```

---

## Future Improvements

### Planned Enhancements

1. **Action Execution**: Allow chatbot to perform actions (create/update/delete)
2. **Multi-language Support**: Support for French, Arabic, etc.
3. **Voice Input**: Speech-to-text integration
4. **Analytics Dashboard**: Chatbot usage analytics
5. **Custom Training**: Fine-tune AI on travel-specific data
6. **Notifications**: Proactive alerts for pending items
7. **Export Functionality**: Export chat conversations
8. **Ticket Creation**: Create support tickets from chat

### Technical Improvements

1. **Caching**: Cache frequently requested data
2. **Streaming**: Stream AI responses for faster perceived performance
3. **WebSocket**: Real-time bidirectional communication
4. **Function Calling**: Use OpenAI function calling for structured data
5. **Vector Database**: Semantic search capabilities

---

## Conclusion

The TravelSmart AI Chatbot represents a modern approach to administrative assistance in travel management systems. By combining the power of large language models (GPT) with real-time database access, it provides administrators with an intelligent, conversational interface that can answer questions, provide analytics, and assist with daily operations.

The architecture is designed to be:

- **Scalable**: Easily add new data sources and capabilities
- **Maintainable**: Clear separation of concerns
- **Extensible**: Ready for future enhancements
- **User-friendly**: Intuitive chat interface

---

## References

- [OpenRouter API Documentation](https://openrouter.ai/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [OpenAI API Reference](https://platform.openai.com/docs/api-reference)

---

_Document Version: 1.0_  
_Last Updated: January 2026_  
_Author: TravelSmart Development Team_
