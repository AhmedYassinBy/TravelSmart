package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.TravelerDto;
import com.ahmedyassin.TravelSmart.entities.TravelRequest;
import com.ahmedyassin.TravelSmart.entities.Traveler;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.repositories.TravelerRepository;
import com.ahmedyassin.TravelSmart.repositories.TravelRequestRepository;
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
public class TravelerService {

    private final TravelerRepository travelerRepository;
    private final TravelRequestRepository travelRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Injected NotificationService

    @Transactional
    public TravelerDto.TravelerResponseDTO createTraveler(TravelerDto.TravelerCreateUpdateDTO createDto) {
        Traveler traveler = new Traveler();
        traveler.setFirstName(createDto.getFirstName());
        traveler.setLastName(createDto.getLastName());
        traveler.setPosition(createDto.getPosition());
        traveler.setPasseportFileName(createDto.getPasseportFileName());
        traveler.setPasseportUploadedAt(createDto.getPasseportUploadedAt());

        TravelRequest travelRequest = travelRequestRepository.findById(createDto.getTravelRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Travel Request not found with ID: " + createDto.getTravelRequestId()));
        traveler.setTravelRequest(travelRequest);

        User linkedUser = null;
        if (createDto.getUserId() != null) {
            linkedUser = userRepository.findById(createDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + createDto.getUserId()));
            traveler.setUser(linkedUser);
        } else {
            traveler.setUser(null);
        }

        Traveler savedTraveler = travelerRepository.save(traveler);

        // Notify the creator of the travel request about the new traveler
        if (travelRequest.getCreator() != null) {
            notificationService.createInternalNotification(
                    travelRequest.getCreator().getId(),
                    savedTraveler.getFirstName() + " " + savedTraveler.getLastName() + " has been added as a traveler to your request (" +
                            travelRequest.getStartDate() + " to " + travelRequest.getEndDate() + ").",
                    false
            );
        }
        // Notify the linked user (if any) that they are now a traveler
        if (linkedUser != null) {
            notificationService.createInternalNotification(
                    linkedUser.getId(),
                    "You have been added as a traveler to a request from " + travelRequest.getStartDate() + " to " + travelRequest.getEndDate() + ".",
                    false
            );
        }

        return TravelerDto.TravelerResponseDTO.fromEntity(savedTraveler);
    }

    @Transactional(readOnly = true)
    public Optional<TravelerDto.TravelerResponseDTO> getTravelerById(UUID id) {
        return travelerRepository.findById(id)
                .map(TravelerDto.TravelerResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<TravelerDto.TravelerResponseDTO> getAllTravelers() {
        return travelerRepository.findAll().stream()
                .map(TravelerDto.TravelerResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public TravelerDto.TravelerResponseDTO updateTraveler(UUID id, TravelerDto.TravelerCreateUpdateDTO updateDto) {
        Traveler existingTraveler = travelerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Traveler not found with ID: " + id));

        String oldPassportFileName = existingTraveler.getPasseportFileName();
        LocalDateTime oldPassportUploadedAt = existingTraveler.getPasseportUploadedAt();

        Optional.ofNullable(updateDto.getFirstName()).ifPresent(existingTraveler::setFirstName);
        Optional.ofNullable(updateDto.getLastName()).ifPresent(existingTraveler::setLastName);
        Optional.ofNullable(updateDto.getPosition()).ifPresent(existingTraveler::setPosition);
        Optional.ofNullable(updateDto.getPasseportFileName()).ifPresent(existingTraveler::setPasseportFileName);
        Optional.ofNullable(updateDto.getPasseportUploadedAt()).ifPresent(existingTraveler::setPasseportUploadedAt);

        UUID oldTravelRequestId = existingTraveler.getTravelRequest() != null ? existingTraveler.getTravelRequest().getId() : null;
        if (updateDto.getTravelRequestId() != null && !updateDto.getTravelRequestId().equals(oldTravelRequestId)) {
            TravelRequest newTravelRequest = travelRequestRepository.findById(updateDto.getTravelRequestId())
                    .orElseThrow(() -> new IllegalArgumentException("Travel Request not found with ID: " + updateDto.getTravelRequestId()));
            existingTraveler.setTravelRequest(newTravelRequest);
            // Notify if traveler moved to a different request
            String travelerName = existingTraveler.getFirstName() + " " + existingTraveler.getLastName();
            if (oldTravelRequestId != null) { // Notify old request creator
                TravelRequest oldRequest = travelRequestRepository.findById(oldTravelRequestId).orElse(null);
                if (oldRequest != null && oldRequest.getCreator() != null) {
                    notificationService.createInternalNotification(oldRequest.getCreator().getId(),
                            travelerName + " has been removed from your travel request (" + oldRequest.getStartDate() + " to " + oldRequest.getEndDate() + ").", false);
                }
            }
            if (newTravelRequest.getCreator() != null) { // Notify new request creator
                notificationService.createInternalNotification(newTravelRequest.getCreator().getId(),
                        travelerName + " has been added to your travel request (" + newTravelRequest.getStartDate() + " to " + newTravelRequest.getEndDate() + ").", false);
            }
        } else if (updateDto.getTravelRequestId() == null && existingTraveler.getTravelRequest() != null) {
            // Traveler unlinked from a request
            TravelRequest oldRequest = existingTraveler.getTravelRequest();
            if (oldRequest.getCreator() != null) {
                notificationService.createInternalNotification(oldRequest.getCreator().getId(),
                        existingTraveler.getFirstName() + " " + existingTraveler.getLastName() + " has been unlinked from your travel request (" + oldRequest.getStartDate() + " to " + oldRequest.getEndDate() + ").", false);
            }
            existingTraveler.setTravelRequest(null);
        }


        UUID oldUserId = existingTraveler.getUser() != null ? existingTraveler.getUser().getId() : null;
        if (updateDto.getUserId() != null && !updateDto.getUserId().equals(oldUserId)) {
            User newUser = userRepository.findById(updateDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + updateDto.getUserId()));
            existingTraveler.setUser(newUser);
            // Notify if traveler linked to a different user
            notificationService.createInternalNotification(newUser.getId(),
                    "You are now linked to a traveler profile (" + existingTraveler.getFirstName() + " " + existingTraveler.getLastName() + ").", false);
            if (oldUserId != null) { // Notify old user if changed
                notificationService.createInternalNotification(oldUserId,
                        "You have been unlinked from a traveler profile (" + existingTraveler.getFirstName() + " " + existingTraveler.getLastName() + ").", false);
            }
        } else if (updateDto.getUserId() == null && existingTraveler.getUser() != null) {
            // User unlinked from traveler
            notificationService.createInternalNotification(existingTraveler.getUser().getId(),
                    "You have been unlinked from the traveler profile (" + existingTraveler.getFirstName() + " " + existingTraveler.getLastName() + ").", false);
            existingTraveler.setUser(null);
        }

        Traveler updatedTraveler = travelerRepository.save(existingTraveler);

        // Notify if passport file name or upload time changes
        if (updateDto.getPasseportFileName() != null && !updateDto.getPasseportFileName().equals(oldPassportFileName) ||
                updateDto.getPasseportUploadedAt() != null && !updateDto.getPasseportUploadedAt().equals(oldPassportUploadedAt)) {

            // Notify the linked user if there is one
            if (updatedTraveler.getUser() != null) {
                notificationService.createInternalNotification(
                        updatedTraveler.getUser().getId(),
                        "Your passport information in your traveler profile has been updated.",
                        false
                );
            }
            // Also notify the travel request creator if this traveler is part of a request
            if (updatedTraveler.getTravelRequest() != null && updatedTraveler.getTravelRequest().getCreator() != null) {
                notificationService.createInternalNotification(
                        updatedTraveler.getTravelRequest().getCreator().getId(),
                        "Passport information for " + updatedTraveler.getFirstName() + " " + updatedTraveler.getLastName() + " has been updated.",
                        false
                );
            }
        }

        return TravelerDto.TravelerResponseDTO.fromEntity(updatedTraveler);
    }

    @Transactional
    public void deleteTraveler(UUID id) {
        if (!travelerRepository.existsById(id)) {
            throw new RuntimeException("Traveler not found with ID: " + id);
        }
        Traveler travelerToDelete = travelerRepository.findById(id).orElse(null);
        if (travelerToDelete != null) {
            // Notify the linked user (if any)
            if (travelerToDelete.getUser() != null) {
                notificationService.createInternalNotification(
                        travelerToDelete.getUser().getId(),
                        "Your traveler profile (" + travelerToDelete.getFirstName() + " " + travelerToDelete.getLastName() + ") has been deleted.",
                        false
                );
            }
            // Notify the creator of the associated travel request (if any)
            if (travelerToDelete.getTravelRequest() != null && travelerToDelete.getTravelRequest().getCreator() != null) {
                notificationService.createInternalNotification(
                        travelerToDelete.getTravelRequest().getCreator().getId(),
                        travelerToDelete.getFirstName() + " " + travelerToDelete.getLastName() + " has been removed from travel request " +
                                travelerToDelete.getTravelRequest().getId() + ".",
                        false
                );
            }
        }
        travelerRepository.deleteById(id);
    }
}