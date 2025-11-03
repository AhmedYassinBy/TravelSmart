package com.ahmedyassin.TravelSmart.controllers;


import com.ahmedyassin.TravelSmart.dto.NotificationDto;
import com.ahmedyassin.TravelSmart.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // --- NO @POST endpoint for direct creation by external clients ---
    // Notifications are created internally by other services (e.g., TravelRequestService)

    @GetMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_RH')")
    public ResponseEntity<NotificationDto.NotificationResponseDTO> getNotificationById(@PathVariable UUID id) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RH')") // Admins/RH can see all notifications
    public List<NotificationDto.NotificationResponseDTO> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/user/{recipientId}")
    // @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_RH')") // Users can see their own, others for management
    public ResponseEntity<List<NotificationDto.NotificationResponseDTO>> getNotificationsByRecipient(
            @PathVariable UUID recipientId,
            @RequestParam(required = false) Boolean isRead) {
        List<NotificationDto.NotificationResponseDTO> notifications = notificationService.getNotificationsByRecipientId(recipientId, isRead);
        if (notifications.isEmpty() && isRead == null) {
            return ResponseEntity.notFound().build(); // No notifications at all for this user
        } else if (notifications.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // No notifications matching read status
        }
        return ResponseEntity.ok(notifications);
    }


    @PutMapping("/{id}/mark-read")
    // @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_RH')") // Users can mark their own as read
    public ResponseEntity<NotificationDto.NotificationResponseDTO> markNotificationAsRead(@PathVariable UUID id) {
        try {
            NotificationDto.NotificationResponseDTO updatedNotification = notificationService.markNotificationAsRead(id);
            return ResponseEntity.ok(updatedNotification);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_RH')") // Only certain roles can delete notifications
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}