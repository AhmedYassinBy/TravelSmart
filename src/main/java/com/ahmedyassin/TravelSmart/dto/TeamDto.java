package com.ahmedyassin.TravelSmart.dto;



import com.ahmedyassin.TravelSmart.entities.Team; // Only if you use Team.fromEntity method
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TeamDto {

    @Getter
    @Setter
    public static class TeamUpdateDTO { // This is for incoming update requests
        private String name;
        private UUID teamLeadId; // This is good for receiving just the ID
    }

    @Getter
    @Setter
    @Builder
    public static class TeamResponseDTO { // This is for outgoing responses
        private UUID id;
        private String name;
        private UserResponseDTO teamLead; // Nested DTO for team lead
        private List<UserResponseDTO> members; // Nested DTOs for members

        // Inner DTO for User, to avoid circular references and expose only necessary fields
        @Getter
        @Setter
        @Builder
        public static class UserResponseDTO {
            private UUID id;
            private String username;
            private String email;
            // Add other user fields you want to expose, but avoid sensitive ones like password
        }

        // Static factory method to convert Team entity to TeamResponseDTO
        public static TeamResponseDTO fromEntity(Team team) {
            if (team == null) {
                return null;
            }

            // Initialize teamLead explicitly if it's lazy-loaded and you want to include it
            UserResponseDTO teamLeadDTO = null;
            if (team.getTeamLead() != null) { // Accessing this will trigger lazy loading if not already loaded
                // It's crucial that team.getTeamLead() is not a proxy here or is initialized
                teamLeadDTO = UserResponseDTO.builder()
                        .id(team.getTeamLead().getId())
                        .username(team.getTeamLead().getUsername())
                        .email(team.getTeamLead().getEmail())
                        .build();
            }

            // Initialize members explicitly if it's lazy-loaded and you want to include them
            List<UserResponseDTO> memberDTOs = null;
            if (team.getMembers() != null) { // Accessing this will trigger lazy loading if not already loaded
                memberDTOs = team.getMembers().stream() // Iterate over the collection to initialize it
                        .map(member -> UserResponseDTO.builder()
                                .id(member.getId())
                                .username(member.getUsername())
                                .email(member.getEmail())
                                .build())
                        .collect(Collectors.toList());
            }

            return TeamResponseDTO.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .teamLead(teamLeadDTO)
                    .members(memberDTOs)
                    .build();
        }
    }
    @Getter
    @Setter
    public static class TeamCreateDTO { // <-- NEW DTO for creation
        private String name;
        private UUID teamLeadId; // Accepts just the UUID string
    }
}
