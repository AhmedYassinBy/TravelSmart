package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.dto.RegisterClientRequest;
import com.ahmedyassin.TravelSmart.dto.UpdateProfileRequest;
import com.ahmedyassin.TravelSmart.entities.Client;
import com.ahmedyassin.TravelSmart.services.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Register a new client (mobile app user)
     */
    @PostMapping("/register")
    public ResponseEntity<Client> registerClient(@RequestBody RegisterClientRequest request) {
        log.info("POST /api/client/register - email: {}", request.getEmail());
        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setPassword(request.getPassword());
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setTelephone(request.getTelephone());
        client.setEnabled(false);

        Client saved = clientService.register(client);
        return ResponseEntity.ok(saved);
    }

    /**
     * Confirm account via token
     */
    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String token) {
        log.info("GET /api/client/confirm - token: {}", token);
        try {
            clientService.confirmAccount(token);
            return ResponseEntity.ok("Account confirmed successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Client login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        log.info("POST /api/client/login - email: {}", body.get("email"));
        try {
            Client client = clientService.login(body.get("email"), body.get("password"));
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            switch (e.getMessage()) {
                case "NO_ACCOUNT":
                    return ResponseEntity.status(404).body("No account associated with this email.");
                case "ACCOUNT_DISABLED":
                    return ResponseEntity.status(403).body("Account not activated. Check your email.");
                case "INVALID_CREDENTIALS":
                    return ResponseEntity.status(400).body("Incorrect email or password.");
                default:
                    return ResponseEntity.badRequest().body("Unknown error.");
            }
        }
    }

    /**
     * Forgot password - send reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> body) {
        log.info("POST /api/client/forgot-password - email: {}", body.get("email"));
        try {
            clientService.forgotPassword(body.get("email"));
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email sent for password reset");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Reset password (POST - for mobile app)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        log.info("POST /api/client/reset-password");
        try {
            String token = body.get("token");
            String newPassword = body.get("newPassword");

            clientService.resetPassword(token, newPassword);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Reset password (GET - for deep link redirect)
     */
    @GetMapping("/reset-password")
    public ResponseEntity<Void> redirectToApp(@RequestParam String token) {
        log.info("GET /api/client/reset-password - redirect for token");
        URI uri = URI.create("voyageproject://reset-password?token=" + token);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Get client profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Client> getProfile(@RequestParam String email) {
        log.info("GET /api/client/profile - email: {}", email);
        Client client = clientService.getProfile(email);
        return ResponseEntity.ok(client);
    }

    /**
     * Update client profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Client> updateProfile(@RequestParam String email,
                                                @RequestBody UpdateProfileRequest request) {
        log.info("PUT /api/client/profile - email: {}", email);
        Client updated = clientService.updateProfile(email, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Change password
     */
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestParam String email,
                                                 @RequestBody Map<String, String> body) {
        log.info("PUT /api/client/change-password - email: {}", email);
        String newPassword = body.get("newPassword");
        clientService.changePassword(email, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }
}
