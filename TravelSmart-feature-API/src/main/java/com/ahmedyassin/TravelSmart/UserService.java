package com.ahmedyassin.TravelSmart;



import com.ahmedyassin.TravelSmart.entities.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User createUser(User user);
    User updateUser(UUID id, Map<String, Object> updates); // Use Map for partial updates
    void deleteUser(UUID id);
    Optional<User> getUserById(UUID id);
    List<User> getAllUsers();
    // Potentially add more methods like findByUsername, findByEmail if needed by other services
    Optional<User> findByUsername(String username);
}