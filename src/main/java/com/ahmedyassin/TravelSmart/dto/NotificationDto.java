package com.ahmedyassin.TravelSmart.dto;


import com.ahmedyassin.TravelSmart.entities.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {

    @Getter
    @Setter
    public static class NotificationCreateUpdateDTO {
        private UUID recipientId; // Mandatory: The user who receives the notification
        private String message;   // Mandatory: The content of the notification
        private boolean isRead;   // Optional: Default to false if not provided
        // createdAt is handled by the entity
    }

    @Getter
    @Setter
    @Builder
    public static class NotificationResponseDTO {
        private UUID id;
        private UUID recipientId;
        private String message;
        private boolean isRead;
        private LocalDateTime createdAt;

        public static NotificationResponseDTO fromEntity(Notification notification) {
            if (notification == null) {
                return null;
            }

            // Safely get recipient ID from lazy-loaded associated entity
            UUID recipientId = null;
            if (notification.getRecipient() != null) {
                recipientId = notification.getRecipient().getId();
            }

            return NotificationResponseDTO.builder()
                    .id(notification.getId())
                    .recipientId(recipientId)
                    .message(notification.getMessage())
                    .isRead(notification.isRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }
}
