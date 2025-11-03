package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.NotificationDto;
import com.ahmedyassin.TravelSmart.entities.Notification;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.repositories.NotificationRepository;
import com.ahmedyassin.TravelSmart.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDto.NotificationResponseDTO createInternalNotification(
            UUID recipientId, String message, boolean isRead) {
        Notification notification = new Notification();

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient User not found with ID: " + recipientId));
        notification.setRecipient(recipient);

        notification.setMessage(message);
        notification.setRead(isRead);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);
        return NotificationDto.NotificationResponseDTO.fromEntity(savedNotification);
    }

    @Transactional(readOnly = true)
    public Optional<NotificationDto.NotificationResponseDTO> getNotificationById(UUID id) {
        return notificationRepository.findById(id)
                .map(NotificationDto.NotificationResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto.NotificationResponseDTO> getAllNotifications() {
        return notificationRepository.findAll().stream()
                .map(NotificationDto.NotificationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto.NotificationResponseDTO> getNotificationsByRecipientId(UUID recipientId, Boolean isRead) {
        List<Notification> notifications;
        if (isRead != null) {
            notifications = notificationRepository.findByRecipientIdAndIsRead(recipientId, isRead);
        } else {
            notifications = notificationRepository.findByRecipientId(recipientId);
        }
        return notifications.stream()
                .map(NotificationDto.NotificationResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto.NotificationResponseDTO markNotificationAsRead(UUID id) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        existingNotification.setRead(true);
        Notification updatedNotification = notificationRepository.save(existingNotification);
        return NotificationDto.NotificationResponseDTO.fromEntity(updatedNotification);
    }

    @Transactional
    public void deleteNotification(UUID id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with ID: " + id);
        }
        notificationRepository.deleteById(id);
    }
}