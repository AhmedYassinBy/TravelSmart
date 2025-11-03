package com.ahmedyassin.TravelSmart.dto;



import com.ahmedyassin.TravelSmart.entities.TravelRequest;
import com.ahmedyassin.TravelSmart.entities.Traveler;
import com.ahmedyassin.TravelSmart.enums.TravelStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate; // Changed from LocalDateTime
import java.time.LocalDateTime; // For createdAt/updatedAt
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class TravelRequestDto {

    @Getter
    @Setter
    public static class TravelRequestCreateUpdateDTO {
        private LocalDate startDate; // Use LocalDate
        private LocalDate endDate;   // Use LocalDate
        private TravelStatus status; // Use TravelStatus enum (optional for create, but good for update)
        private UUID teamId;         // Required for creation/update
        private UUID creatorId;      // Required for creation/update
        private String rhComments;
        private String optimizationDetails;
        private String pays;

    }

    @Getter
    @Setter
    @Builder
    public static class TravelRequestResponseDTO {
        private UUID id;
        private LocalDate startDate;
        private LocalDate endDate;
        private TravelStatus status;
        private UUID teamId;
        private UUID creatorId;
        private String rhComments;
        private String optimizationDetails;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String pays;
        // Optionally, include basic traveler info if you want to display them with the request
         private List<TravelerDto.TravelerResponseDTO> travelers; // This would require TravelerDto.TravelerResponseDTO

        public static TravelRequestResponseDTO fromEntity(TravelRequest travelRequest) {
            if (travelRequest == null) {
                return null;
            }

            // Safely get IDs from lazy-loaded associated entities
            UUID teamId = null;
            if (travelRequest.getTeam() != null) {
                teamId = travelRequest.getTeam().getId();
            }

            UUID creatorId = null;
            if (travelRequest.getCreator() != null) {
                creatorId = travelRequest.getCreator().getId();
            }

            return TravelRequestResponseDTO.builder()
                    .id(travelRequest.getId())
                    .startDate(travelRequest.getStartDate())
                    .endDate(travelRequest.getEndDate())
                    .status(travelRequest.getStatus())
                    .teamId(teamId)
                    .creatorId(creatorId)
                    .rhComments(travelRequest.getRhComments())
                    .optimizationDetails(travelRequest.getOptimizationDetails())
                    .createdAt(travelRequest.getCreatedAt())
                    .updatedAt(travelRequest.getUpdatedAt())
                    .pays(travelRequest.getPays())
                    // If you want to include travelers, uncomment this and ensure TravelerDto is imported:
                    .travelers(travelRequest.getTravelers() != null ?
                                travelRequest.getTravelers().stream()
                                .map(TravelerDto.TravelerResponseDTO::fromEntity)
                               .collect(Collectors.toList()) : null)
                    .build();
        }
    }
}
