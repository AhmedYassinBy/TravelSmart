package com.ahmedyassin.TravelSmart.controllers.external;

import com.ahmedyassin.TravelSmart.dto.external.AirportDTO;
import com.ahmedyassin.TravelSmart.services.external.AirportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AirportController {

    private final AirportService airportService;

    /**
     * Rechercher des aéroports
     * Example: GET /api/airports/search?airportName=Charles&country=France
     */
    @GetMapping("/search")
    public ResponseEntity<List<AirportDTO>> searchAirports(
            @RequestParam(required = false) String airportName,
            @RequestParam(required = false) String country) {
        log.info("REST Request to search airports with name: {} in country: {}", airportName, country);
        List<AirportDTO> airports = airportService.searchAirports(airportName, country);
        return ResponseEntity.ok(airports);
    }
}

