package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.dto.ChatRequest;
import com.ahmedyassin.TravelSmart.dto.ChatResponse;
import com.ahmedyassin.TravelSmart.services.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
