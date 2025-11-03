package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.TravelRequestDto;
import com.ahmedyassin.TravelSmart.entities.TravelRequest;
import com.ahmedyassin.TravelSmart.entities.Team;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.entities.Traveler; // Import the Traveler entity
import com.ahmedyassin.TravelSmart.enums.TravelStatus;
import com.ahmedyassin.TravelSmart.repositories.TravelRequestRepository;
import com.ahmedyassin.TravelSmart.repositories.TeamRepository;
import com.ahmedyassin.TravelSmart.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList; // Needed for ArrayList
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final GeminiAIService geminiAIService; // ⭐ Inject GeminiAIService ⭐

    @Transactional
    public TravelRequestDto.TravelRequestResponseDTO createTravelRequest(TravelRequestDto.TravelRequestCreateUpdateDTO createDto) {
        TravelRequest travelRequest = new TravelRequest();

        travelRequest.setStartDate(createDto.getStartDate());
        travelRequest.setEndDate(createDto.getEndDate());
        // ⭐ Add the 'pays' field mapping here ⭐
        travelRequest.setPays(createDto.getPays()); // <--- ADD THIS LINE!

        travelRequest.setStatus(TravelStatus.CREATED); // Set to PENDING as AI optimization will follow

        Team team = teamRepository.findById(createDto.getTeamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + createDto.getTeamId()));
        travelRequest.setTeam(team);

        User creator = userRepository.findById(createDto.getCreatorId())
                .orElseThrow(() -> new IllegalArgumentException("Creator User not found with ID: " + createDto.getCreatorId()));
        travelRequest.setCreator(creator);

        travelRequest.setRhComments(createDto.getRhComments());
        // ⭐ Optimization details initially null; Gemini will fill this ⭐
        travelRequest.setOptimizationDetails(null);

        // ⭐ Automatically add the creator as a traveler ⭐
        List<Traveler> travelers = new ArrayList<>();
        Traveler creatorAsTraveler = new Traveler();
        creatorAsTraveler.setUser(creator);
        creatorAsTraveler.setTravelRequest(travelRequest); // Link back to the request
        travelers.add(creatorAsTraveler);
        travelRequest.setTravelers(travelers); // Set the populated list of travelers

        TravelRequest savedTravelRequest = travelRequestRepository.save(travelRequest);
        System.out.println("TravelRequest saved with ID: " + savedTravelRequest.getId() + ". Status: " + savedTravelRequest.getStatus());

        // Notify the creator that their travel request has been submitted
        notificationService.createInternalNotification(
                savedTravelRequest.getCreator().getId(),
                "Your travel request from " + savedTravelRequest.getStartDate() + " to " +
                        savedTravelRequest.getEndDate() + " has been submitted with status: " + savedTravelRequest.getStatus().name() + ". We're now generating the best opportunities for you.",
                false
        );

        // ⭐ ASYNCHRONOUS CALL TO GEMINI AI SERVICE ⭐
        geminiAIService.generateTravelPlan(savedTravelRequest)
                .subscribe(
                        // On successful response from Gemini
                        geminiPlanJson -> {
                            // Re-fetch the entity to ensure it's up-to-date before modification
                            Optional<TravelRequest> updatedRequestOpt = travelRequestRepository.findById(savedTravelRequest.getId());
                            if (updatedRequestOpt.isPresent()) {
                                TravelRequest requestToUpdate = updatedRequestOpt.get();
                                requestToUpdate.setOptimizationDetails(geminiPlanJson); // Set Gemini's JSON
                                requestToUpdate.setStatus(TravelStatus.INFO_CONFIRMED); // Update status to reflect AI processing
                                travelRequestRepository.save(requestToUpdate); // Save the updated request
                                System.out.println("Gemini optimization details saved for request " + requestToUpdate.getId() + ". Status: " + requestToUpdate.getStatus());

                                // Notify creator that optimization is complete
                                notificationService.createInternalNotification(
                                        requestToUpdate.getCreator().getId(),
                                        "Your travel request from " + requestToUpdate.getStartDate() + " to " +
                                                requestToUpdate.getEndDate() + " has been optimized! Check out the details.",
                                        false
                                );
                            } else {
                                System.err.println("Error: Travel request " + savedTravelRequest.getId() + " could not be reloaded for AI update after Gemini call.");
                            }
                        },
                        // On error from Gemini API call
                        error -> {
                            System.err.println("Failed to generate opportunities with Gemini for request " + savedTravelRequest.getId() + ": " + error.getMessage());
                            // Re-fetch the entity to update status and add comments on error
                            Optional<TravelRequest> requestOnErrorOpt = travelRequestRepository.findById(savedTravelRequest.getId());
                            if (requestOnErrorOpt.isPresent()) {
                                TravelRequest requestToUpdate = requestOnErrorOpt.get();
                                requestToUpdate.setStatus(TravelStatus.INFO_INCOMPLETE); // Set error status
                                // Truncate error message if too long for rhComments column
                                requestToUpdate.setRhComments("AI generation failed: " + error.getMessage().substring(0, Math.min(error.getMessage().length(), 250)) + "...");
                                travelRequestRepository.save(requestToUpdate);
                                System.err.println("Request " + requestToUpdate.getId() + " status updated to FAILED_OPTIMIZATION.");

                                notificationService.createInternalNotification(
                                        requestToUpdate.getCreator().getId(),
                                        "Travel plan generation for your request from " + requestToUpdate.getStartDate() + " to " +
                                                requestToUpdate.getEndDate() + " failed. Please contact the HR department.",
                                        false
                                );
                            }
                        }
                );

        // Return the DTO immediately. The optimization will happen in the background.
        return TravelRequestDto.TravelRequestResponseDTO.fromEntity(savedTravelRequest);
    }

    @Transactional(readOnly = true)
    public Optional<TravelRequestDto.TravelRequestResponseDTO> getTravelRequestById(UUID id) {
        return travelRequestRepository.findById(id)
                .map(travelRequest -> {
                    // ⭐ Ensure lazy-loaded relationships are fetched ⭐
                    if (travelRequest.getTeam() != null) travelRequest.getTeam().getName();
                    if (travelRequest.getCreator() != null) travelRequest.getCreator().getUsername();
                    // Force loading travelers as they are used in Gemini prompt
                    if (travelRequest.getTravelers() != null) travelRequest.getTravelers().size(); // Access to initialize
                    return TravelRequestDto.TravelRequestResponseDTO.fromEntity(travelRequest);
                });
    }

    @Transactional(readOnly = true)
    public List<TravelRequestDto.TravelRequestResponseDTO> getAllTravelRequests() {
        return travelRequestRepository.findAll().stream()
                .map(travelRequest -> {
                    // ⭐ Ensure lazy-loaded relationships are fetched ⭐
                    if (travelRequest.getTeam() != null) travelRequest.getTeam().getName();
                    if (travelRequest.getCreator() != null) travelRequest.getCreator().getUsername();
                    // Force loading travelers as they are used in Gemini prompt
                    if (travelRequest.getTravelers() != null) travelRequest.getTravelers().size(); // Access to initialize
                    return TravelRequestDto.TravelRequestResponseDTO.fromEntity(travelRequest);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TravelRequestDto.TravelRequestResponseDTO updateTravelRequest(UUID id, TravelRequestDto.TravelRequestCreateUpdateDTO updateDto) {
        TravelRequest existingTravelRequest = travelRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Travel Request not found with ID: " + id));

        TravelStatus oldStatus = existingTravelRequest.getStatus();

        Optional.ofNullable(updateDto.getStartDate()).ifPresent(existingTravelRequest::setStartDate);
        Optional.ofNullable(updateDto.getEndDate()).ifPresent(existingTravelRequest::setEndDate);
        Optional.ofNullable(updateDto.getStatus()).ifPresent(existingTravelRequest::setStatus);
        Optional.ofNullable(updateDto.getRhComments()).ifPresent(existingTravelRequest::setRhComments);
        Optional.ofNullable(updateDto.getOptimizationDetails()).ifPresent(existingTravelRequest::setOptimizationDetails);
        // ⭐ Add the 'pays' field mapping for updates as well ⭐
        Optional.ofNullable(updateDto.getPays()).ifPresent(existingTravelRequest::setPays); // <--- ADD THIS LINE!

        UUID oldTeamId = existingTravelRequest.getTeam() != null ? existingTravelRequest.getTeam().getId() : null;
        if (updateDto.getTeamId() != null && !updateDto.getTeamId().equals(oldTeamId)) {
            Team newTeam = teamRepository.findById(updateDto.getTeamId())
                    .orElseThrow(() -> new IllegalArgumentException("Team not found with ID: " + updateDto.getTeamId()));
            existingTravelRequest.setTeam(newTeam);
            // Notify if team changes
            notificationService.createInternalNotification(
                    existingTravelRequest.getCreator().getId(),
                    "Your travel request (" + existingTravelRequest.getStartDate() + " to " + existingTravelRequest.getEndDate() + ") has been moved to a new team.",
                    false
            );
        }

        UUID oldCreatorId = existingTravelRequest.getCreator() != null ? existingTravelRequest.getCreator().getId() : null;
        if (updateDto.getCreatorId() != null && !updateDto.getCreatorId().equals(oldCreatorId)) {
            User newCreator = userRepository.findById(updateDto.getCreatorId())
                    .orElseThrow(() -> new IllegalArgumentException("Creator User not found with ID: " + updateDto.getCreatorId()));
            existingTravelRequest.setCreator(newCreator);
            // Notify if creator changes (this is rare, mostly for admin re-assignment)
            notificationService.createInternalNotification(
                    newCreator.getId(),
                    "You are now the creator for a travel request (" + existingTravelRequest.getStartDate() + " to " + existingTravelRequest.getEndDate() + ").",
                    false
            );
            if (oldCreatorId != null) { // Notify old creator if changed
                notificationService.createInternalNotification(
                        oldCreatorId,
                        "You are no longer the creator for travel request from " + existingTravelRequest.getStartDate() + " to " + existingTravelRequest.getEndDate() + ".",
                        false
                );
            }
        }

        TravelRequest updatedTravelRequest = travelRequestRepository.save(existingTravelRequest);

        // ⭐ Notification for Travel Status Change ⭐
        if (updateDto.getStatus() != null && oldStatus != updatedTravelRequest.getStatus()) {
            String message = "Your travel request from " + updatedTravelRequest.getStartDate() +
                    " to " + updatedTravelRequest.getEndDate() + " status has changed to: " + updatedTravelRequest.getStatus().name() + ".";
            notificationService.createInternalNotification(
                    updatedTravelRequest.getCreator().getId(),
                    message,
                    false
            );

            // Specific notifications for RH comments or optimization details if they are added/updated by RH
            if (updateDto.getRhComments() != null && !updateDto.getRhComments().equals(existingTravelRequest.getRhComments())) {
                notificationService.createInternalNotification(
                        updatedTravelRequest.getCreator().getId(),
                        "HR/RH comments added to your travel request: " + updateDto.getRhComments(),
                        false
                );
            }
            if (updateDto.getOptimizationDetails() != null && !updateDto.getOptimizationDetails().equals(existingTravelRequest.getOptimizationDetails())) {
                notificationService.createInternalNotification(
                        updatedTravelRequest.getCreator().getId(),
                        "Optimization details added to your travel request: " + updateDto.getOptimizationDetails(),
                        false
                );
            }
        }

        return TravelRequestDto.TravelRequestResponseDTO.fromEntity(updatedTravelRequest);
    }

    @Transactional
    public void deleteTravelRequest(UUID id) {
        if (!travelRequestRepository.existsById(id)) {
            throw new RuntimeException("Travel Request not found with ID: " + id);
        }
        // Fetch the request before deleting to get creator ID for notification
        TravelRequest requestToDelete = travelRequestRepository.findById(id).orElse(null);
        if (requestToDelete != null && requestToDelete.getCreator() != null) {
            notificationService.createInternalNotification(
                    requestToDelete.getCreator().getId(),
                    "Your travel request from " + requestToDelete.getStartDate() + " to " + requestToDelete.getEndDate() + " has been deleted.",
                    false
            );
        }
        travelRequestRepository.deleteById(id);
    }
}