package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.dto.TeamDto; // Import your DTO class
import com.ahmedyassin.TravelSmart.entities.Team; // Still needed for addTeam input
import com.ahmedyassin.TravelSmart.services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("teams")
@RestController
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping("")
    public ResponseEntity<TeamDto.TeamResponseDTO> addTeam(@RequestBody TeamDto.TeamCreateDTO createDto) {

        System.out.println("Controller received TeamCreateDTO: " + createDto.getName() + ", TeamLeadId: " + createDto.getTeamLeadId());
        try {
            TeamDto.TeamResponseDTO responseDto = teamService.createTeam(createDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            // Return error message for better debugging
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/{id}")
     @PreAuthorize("hasAnyRole('ROLE_RH')")
    public ResponseEntity<TeamDto.TeamResponseDTO> getTeamById(@PathVariable UUID id) {
        return teamService.getTeamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("")
    // @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_USER')")
    public List<TeamDto.TeamResponseDTO> getAllTeams() { // Changed return type
        return teamService.getAllTeams();
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_RH')")
    public ResponseEntity<?> updateTeam(@PathVariable UUID id, @RequestBody TeamDto.TeamUpdateDTO updateDto) {
        try {
            TeamDto.TeamResponseDTO updatedTeam = teamService.updateTeam(id, updateDto); // Changed return type
            return ResponseEntity.ok(updatedTeam);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating team: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('ROLE_RH')")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID id) {
        try {
            teamService.deleteTeam(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
