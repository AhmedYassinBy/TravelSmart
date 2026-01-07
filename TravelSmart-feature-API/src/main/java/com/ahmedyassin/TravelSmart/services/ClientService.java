package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.UpdateProfileRequest;
import com.ahmedyassin.TravelSmart.entities.Client;
import com.ahmedyassin.TravelSmart.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.mobile.base-url:http://192.168.0.36:8085}")
    private String mobileBaseUrl;

    // Temporary token storage (use database in production)
    private final Map<String, String> confirmationTokens = new HashMap<>();
    private final Map<String, String> resetTokens = new HashMap<>();

    /**
     * Register a new client
     */
    public Client register(Client client) {
        if (clientRepository.findByEmail(client.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        client.setEnabled(false);
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        Client saved = clientRepository.save(client);

        // Generate confirmation token
        String token = UUID.randomUUID().toString();
        confirmationTokens.put(token, client.getEmail());

        // Send confirmation email
        String link = mobileBaseUrl + "/api/client/confirm?token=" + token;
        try {
            emailService.sendConfirmationEmail(client.getEmail(), link);
        } catch (Exception e) {
            log.warn("Failed to send confirmation email: {}", e.getMessage());
        }

        return saved;
    }

    /**
     * Confirm client account
     */
    public boolean confirmAccount(String token) {
        String email = confirmationTokens.get(token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setEnabled(true);
        clientRepository.save(client);
        confirmationTokens.remove(token);
        return true;
    }

    /**
     * Handle forgot password request
     */
    public boolean forgotPassword(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, client.getEmail());

        String link = mobileBaseUrl + "/api/client/reset-password?token=" + token;
        try {
            emailService.sendPasswordResetEmail(email, link);
        } catch (Exception e) {
            log.warn("Failed to send password reset email: {}", e.getMessage());
        }
        return true;
    }

    /**
     * Reset password with token
     */
    public boolean resetPassword(String token, String newPassword) {
        String email = resetTokens.get(token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired token");
        }

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepository.save(client);
        resetTokens.remove(token);
        return true;
    }

    /**
     * Client login
     */
    public Client login(String email, String password) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("NO_ACCOUNT"));

        if (!client.isEnabled()) {
            throw new RuntimeException("ACCOUNT_DISABLED");
        }

        if (!passwordEncoder.matches(password, client.getPassword())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        return client;
    }

    /**
     * Get client profile
     */
    public Client getProfile(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    /**
     * Update client profile
     */
    public Client updateProfile(String email, UpdateProfileRequest request) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (request.getFirstName() != null) {
            client.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            client.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            client.setEmail(request.getEmail());
        }
        if (request.getTelephone() != null) {
            client.setTelephone(request.getTelephone());
        }

        return clientRepository.save(client);
    }

    /**
     * Change password
     */
    public void changePassword(String email, String newPassword) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        client.setPassword(passwordEncoder.encode(newPassword));
        clientRepository.save(client);
    }
}
