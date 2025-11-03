// src/main/java/com/ahmedyassin/TravelSmart/controllers/TravelerController.java
package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.dto.TravelerDto;
import com.ahmedyassin.TravelSmart.services.TravelerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/travelers") // Standard API endpoint prefix
@RequiredArgsConstructor
public class TravelerController {

    private final TravelerService travelerService;

    @PostMapping
    // @PreAuthorize("hasRole('ROLE_RH')") // Add security as needed
    public ResponseEntity<TravelerDto.TravelerResponseDTO> createTraveler(@RequestBody TravelerDto.TravelerCreateUpdateDTO createDto) {
        try {
            TravelerDto.TravelerResponseDTO newTraveler = travelerService.createTraveler(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTraveler);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Or include e.getMessage()
        }
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_USER')")
    public ResponseEntity<TravelerDto.TravelerResponseDTO> getTravelerById(@PathVariable UUID id) {
        return travelerService.getTravelerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    // @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_USER')")
    public List<TravelerDto.TravelerResponseDTO> getAllTravelers() {
        return travelerService.getAllTravelers();
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_RH')")
    public ResponseEntity<?> updateTraveler(@PathVariable UUID id, @RequestBody TravelerDto.TravelerCreateUpdateDTO updateDto) {
        try {
            TravelerDto.TravelerResponseDTO updatedTraveler = travelerService.updateTraveler(id, updateDto);
            return ResponseEntity.ok(updatedTraveler);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) { // For "Traveler not found"
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating traveler: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_RH')")
    public ResponseEntity<Void> deleteTraveler(@PathVariable UUID id) {
        try {
            travelerService.deleteTraveler(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}