package com.ahmedyassin.TravelSmart.controllers.external;

import com.ahmedyassin.TravelSmart.dto.external.FlightDTO;
import com.ahmedyassin.TravelSmart.services.external.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FlightController {

    private final FlightService flightService;

    /**
     * Rechercher des vols
     * Example: GET /api/flights/search?origin=CDG&destination=JFK&departureDate=2024-12-31&adults=1
     */
    @GetMapping("/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam(required = false, defaultValue = "1") Integer adults) {
        log.info("REST Request to search flights from {} to {} on {}", origin, destination, departureDate);
        List<FlightDTO> flights = flightService.searchFlights(origin, destination, departureDate, adults);
        return ResponseEntity.ok(flights);
    }
}

