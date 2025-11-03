package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.enums.Role;
import com.ahmedyassin.TravelSmart.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for consistency

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

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

        // Notify the new user upon creation
        notificationService.createInternalNotification(
                savedUser.getId(),
                "Welcome to TravelSmart, " + savedUser.getFirstName() + "! Your account has been created successfully.",
                false
        );
        // Also notify an admin/RH about a new user? (Optional, requires admin user ID)
        // UUID adminId = ...;
        // notificationService.createInternalNotification(adminId, "New user '" + savedUser.getUsername() + "' registered.", false);

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

                        if (oldRole != existingUser.getRole()) {
                            notificationService.createInternalNotification(
                                    existingUser.getId(),
                                    "Your role has been updated to: " + existingUser.getRole().name(),
                                    false
                            );
                        }
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role provided: " + value);
                    }
                    break;
                case "password":
                    existingUser.setPassword(passwordEncoder.encode((String) value));

                    notificationService.createInternalNotification(
                            existingUser.getId(),
                            "Your password has been changed. If this wasn't you, please contact support.",
                            false
                    );
                    break;
            }
        });
        User updatedUser = userRepository.save(existingUser);

         notificationService.createInternalNotification(updatedUser.getId(), "Your profile has been updated.", false);
        return updatedUser;
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete != null) {

            notificationService.createInternalNotification(
                    userToDelete.getId(),
                    "Your account has been scheduled for deletion.",
                    false
            );
        }
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
}