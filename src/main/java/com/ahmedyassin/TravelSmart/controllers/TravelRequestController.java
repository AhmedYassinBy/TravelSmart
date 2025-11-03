package com.ahmedyassin.TravelSmart.controllers;


import com.ahmedyassin.TravelSmart.dto.TravelRequestDto;
import com.ahmedyassin.TravelSmart.services.TravelRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/travel-requests")
@RequiredArgsConstructor
public class TravelRequestController {

    private final TravelRequestService travelRequestService;

    @PostMapping
    // @PreAuthorize("hasRole('ROLE_USER')") // Only a user (creator) can create
    public ResponseEntity<TravelRequestDto.TravelRequestResponseDTO> createTravelRequest(@RequestBody TravelRequestDto.TravelRequestCreateUpdateDTO createDto) {
        try {
            TravelRequestDto.TravelRequestResponseDTO newRequest = travelRequestService.createTravelRequest(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Or include error message: .body(e.getMessage())
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_RH')")
    public ResponseEntity<TravelRequestDto.TravelRequestResponseDTO> getTravelRequestById(@PathVariable UUID id) {
        return travelRequestService.getTravelRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    // @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_RH')")
    public List<TravelRequestDto.TravelRequestResponseDTO> getAllTravelRequests() {
        return travelRequestService.getAllTravelRequests();
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> updateTravelRequest(@PathVariable UUID id, @RequestBody TravelRequestDto.TravelRequestCreateUpdateDTO updateDto) {
        try {
            TravelRequestDto.TravelRequestResponseDTO updatedRequest = travelRequestService.updateTravelRequest(id, updateDto);
            return ResponseEntity.ok(updatedRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating travel request: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_RH')")
    public ResponseEntity<Void> deleteTravelRequest(@PathVariable UUID id) {
        try {
            travelRequestService.deleteTravelRequest(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Catch exceptions like Foreign Key Constraint violation if Travelers still linked
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}