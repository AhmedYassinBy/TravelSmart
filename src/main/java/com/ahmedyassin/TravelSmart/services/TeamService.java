package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.dto.TeamDto;
import com.ahmedyassin.TravelSmart.entities.Team;
import com.ahmedyassin.TravelSmart.entities.User;
import com.ahmedyassin.TravelSmart.repositories.TeamRepository;
import com.ahmedyassin.TravelSmart.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Injected NotificationService

    @Transactional
    public TeamDto.TeamResponseDTO createTeam(TeamDto.TeamCreateDTO createDto) {
        Team team = new Team();
        team.setName(createDto.getName());

        // Add a log here to see the DTO in the service
        System.out.println("Service processing TeamCreateDTO: Name=" + createDto.getName() + ", TeamLeadId=" + createDto.getTeamLeadId());

        if (createDto.getTeamLeadId() != null) {
            Optional<User> teamLeadOptional = userRepository.findById(createDto.getTeamLeadId());
            if (teamLeadOptional.isPresent()) {
                User teamLead = teamLeadOptional.get();
                team.setTeamLead(teamLead);
                System.out.println("Service: Found TeamLead User: " + teamLead.getUsername() + " (" + teamLead.getId() + ")");
            } else {
                System.err.println("Service: ERROR - Team lead user NOT found with ID: " + createDto.getTeamLeadId());
                throw new IllegalArgumentException("Team lead user not found with id: " + createDto.getTeamLeadId());
            }
        } else {
            System.out.println("Service: TeamLeadId is null in DTO. Creating team without a team lead.");
        }

        try {
            Team savedTeam = teamRepository.save(team);
            System.out.println("Service: Saved Team - ID: " + savedTeam.getId() + ", Name: " + savedTeam.getName() + ", TeamLead: " + (savedTeam.getTeamLead() != null ? savedTeam.getTeamLead().getUsername() : "null"));

            if (savedTeam.getTeamLead() != null) {
                notificationService.createInternalNotification(
                        savedTeam.getTeamLead().getId(),
                        "You have been assigned as the Team Lead for the new team: " + savedTeam.getName() + ".",
                        false
                );
                System.out.println("Service: Notification sent to team lead: " + savedTeam.getTeamLead().getId());
            } else {
                System.out.println("Service: No team lead assigned, no notification sent for new team creation.");
            }

            if (savedTeam.getMembers() != null) {
                savedTeam.getMembers().size(); // Forces initialization
            }
            return TeamDto.TeamResponseDTO.fromEntity(savedTeam);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Service: Data integrity violation when saving team: " + e.getMessage());
            throw new IllegalArgumentException("Team name '" + team.getName() + "' already exists.");
        }
    }
    @Transactional(readOnly = true) // The transaction is active here
    public Optional<TeamDto.TeamResponseDTO> getTeamById(UUID id) {
        return teamRepository.findById(id)
                .map(team -> {
                    // Access lazy fields here while session is open
                    // This will force their loading if they are needed for the DTO
                    if (team.getTeamLead() != null) {
                        team.getTeamLead().getId(); // Access to initialize proxy
                    }
                    if (team.getMembers() != null) {
                        team.getMembers().size(); // Access to initialize collection
                    }
                    return TeamDto.TeamResponseDTO.fromEntity(team);
                });
    }

    @Transactional(readOnly = true) // The transaction is active here
    public List<TeamDto.TeamResponseDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(team -> {
                    // Same logic as above for each team
                    if (team.getTeamLead() != null) {
                        team.getTeamLead().getId();
                    }
                    if (team.getMembers() != null) {
                        team.getMembers().size();
                    }
                    return TeamDto.TeamResponseDTO.fromEntity(team);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TeamDto.TeamResponseDTO updateTeam(UUID id, TeamDto.TeamUpdateDTO updateDto) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + id));

        // Capture old team lead ID for notification logic
        UUID oldTeamLeadId = existingTeam.getTeamLead() != null ? existingTeam.getTeamLead().getId() : null;

        if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty()) {
            existingTeam.setName(updateDto.getName());
        }

        if (updateDto.getTeamLeadId() != null) {
            User newTeamLead = userRepository.findById(updateDto.getTeamLeadId())
                    .orElseThrow(() -> new IllegalArgumentException("Team lead user not found with id: " + updateDto.getTeamLeadId()));
            existingTeam.setTeamLead(newTeamLead);

            // Notify if team lead has changed
            if (!newTeamLead.getId().equals(oldTeamLeadId)) {
                // Notify the new team lead
                notificationService.createInternalNotification(
                        newTeamLead.getId(),
                        "You are now the Team Lead for team: " + existingTeam.getName() + ".",
                        false
                );
                // Notify the old team lead if they were replaced
                if (oldTeamLeadId != null) {
                    notificationService.createInternalNotification(
                            oldTeamLeadId,
                            "You are no longer the Team Lead for team: " + existingTeam.getName() + ".",
                            false
                    );
                }
            }
        } else if (updateDto.getTeamLeadId() == null && existingTeam.getTeamLead() != null) {
            // Team lead unassigned
            notificationService.createInternalNotification(
                    existingTeam.getTeamLead().getId(),
                    "You have been unassigned as the Team Lead for team: " + existingTeam.getName() + ".",
                    false
            );
            existingTeam.setTeamLead(null); // Allows unassigning the team lead
        }

        try {
            Team updatedTeam = teamRepository.save(existingTeam);
            // Generic notification for team name change if not covered by lead change
            if (updateDto.getName() != null && !updateDto.getName().trim().isEmpty() && !updateDto.getName().equals(updatedTeam.getName())) {
                if (updatedTeam.getTeamLead() != null) {
                    notificationService.createInternalNotification(
                            updatedTeam.getTeamLead().getId(),
                            "The name of your team has been updated to: " + updatedTeam.getName() + ".",
                            false
                    );
                }
            }
            return TeamDto.TeamResponseDTO.fromEntity(updatedTeam);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Update failed: Team name '" + existingTeam.getName() + "' already exists or team lead is already assigned.", e);
        }
    }

    @Transactional
    public void deleteTeam(UUID id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Team not found with id: " + id);
        }
        // Notify the team lead (if any) that their team has been deleted
        Team teamToDelete = teamRepository.findById(id).orElse(null);
        if (teamToDelete != null && teamToDelete.getTeamLead() != null) {
            notificationService.createInternalNotification(
                    teamToDelete.getTeamLead().getId(),
                    "Your team '" + teamToDelete.getName() + "' has been deleted.",
                    false
            );
        }
        teamRepository.deleteById(id);
    }
}