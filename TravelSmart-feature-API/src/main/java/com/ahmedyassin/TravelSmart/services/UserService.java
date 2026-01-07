package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.PasswordResetToken;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.enums.Role;
import com.ahmedyassin.TravelSmart.repositories.PasswordResetTokenRepository;
import com.ahmedyassin.TravelSmart.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for consistency

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:${spring.mail.username:no-reply@travelsmart.com}}")
    private String mailFrom;

    // Frontend base URL used in reset password link (configurable)
    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    @Transactional
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        User savedUser = userRepository.save(user);


        return savedUser;
    }

    @Transactional
    public User updateUser(UUID id, Map<String, Object> updates) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        updates.forEach((key, value) -> {
            switch (key) {
                case "username":
                    existingUser.setUsername((String) value);
                    break;
                case "email":
                    existingUser.setEmail((String) value);
                    break;
                case "firstName":
                    existingUser.setFirstName((String) value);
                    break;
                case "lastName":
                    existingUser.setLastName((String) value);
                    break;
                case "role":
                    Role oldRole = existingUser.getRole();
                    try {
                        existingUser.setRole(Role.valueOf((String) value));


                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role provided: " + value);
                    }
                    break;
                case "password":
                    existingUser.setPassword(passwordEncoder.encode((String) value));


                    break;
            }
        });
        User updatedUser = userRepository.save(existingUser);

                 return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        User userToDelete = userRepository.findById(id).orElse(null);

        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(UUID id) {

        // notificationService.createInternalNotification(id,"You have viewed your profile.",true); // Mark as read immediately?
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // --- Password reset methods ---

    @Transactional
    public String requestPasswordReset(String email) {
        log.info("Password reset requested for email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with that email."));

        // Generate a token (UUID) and expiry 1 hour from now
        String token = UUID.randomUUID().toString();
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(user);
        prt.setExpiresAt(LocalDateTime.now().plusHours(1));

        passwordResetTokenRepository.save(prt);

        // Send email with the reset link
        try {
            String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setFrom(mailFrom.trim());
            mailMessage.setSubject("Password Reset Request - TravelSmart");
            mailMessage.setText("Hello " + user.getFirstName() + ",\n\n" +
                    "We received a request to reset your password. Click the link below to reset it:\n" +
                    resetLink + "\n\n" +
                    "If you did not request this, please ignore this email. The link will expire in 1 hour.\n\n" +
                    "Regards,\nTravelSmart Team");
            mailSender.send(mailMessage);
            log.info("Password reset email sent to {} (from {}).", user.getEmail(), mailFrom);
            log.debug("Generated password reset token for user {}: {}", user.getUsername(), token);
        } catch (Exception e) {
            // Log full stacktrace for debugging
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage(), e);
        }

        // In a real app we don't return the token. For testing we still return it from this method.
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token."));

        if (prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(prt);
            throw new IllegalArgumentException("Token expired.");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the token after successful reset
        passwordResetTokenRepository.delete(prt);
    }
}
