// src/main/java/com/ahmedyassin/TravelSmart/dto/TravelerDto.java
package com.ahmedyassin.TravelSmart.dto;

import com.ahmedyassin.TravelSmart.entities.Traveler;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class TravelerDto {

    @Getter
    @Setter
    public static class TravelerCreateUpdateDTO {
        // For creation and update, we receive the IDs of related entities
        private UUID travelRequestId;
        private UUID userId;
        private String firstName;
        private String lastName;
        private String position;
        private String passeportFileName;
        private LocalDateTime passeportUploadedAt;
    }

    @Getter
    @Setter
    @Builder
    public static class TravelerResponseDTO {
        private UUID id;
        private UUID travelRequestId; // We expose just the ID for related entities in the response
        private UUID userId;
        private String firstName;
        private String lastName;
        private String position;
        private String passeportFileName;
        private LocalDateTime passeportUploadedAt;

        // Static factory method to convert Traveler entity to TravelerResponseDTO
        public static TravelerResponseDTO fromEntity(Traveler traveler) {
            if (traveler == null) {
                return null;
            }

            // Safely get IDs from potentially lazy-loaded associated entities
            UUID travelRequestId = null;
            if (traveler.getTravelRequest() != null) {
                // Accessing getId() on a lazy-loaded proxy is generally safe for just the ID
                travelRequestId = traveler.getTravelRequest().getId();
            }

            UUID userId = null;
            if (traveler.getUser() != null) {
                userId = traveler.getUser().getId();
            }

            return TravelerResponseDTO.builder()
                    .id(traveler.getId())
                    .travelRequestId(travelRequestId)
                    .userId(userId)
                    .firstName(traveler.getFirstName())
                    .lastName(traveler.getLastName())
                    .position(traveler.getPosition())
                    .passeportFileName(traveler.getPasseportFileName())
                    .passeportUploadedAt(traveler.getPasseportUploadedAt())
                    .build();
        }
    }
}