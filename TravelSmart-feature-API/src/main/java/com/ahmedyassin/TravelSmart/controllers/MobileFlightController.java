package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.Flight;
import com.ahmedyassin.TravelSmart.services.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Mobile-facing Flight Controller
 * Provides read-only access to local database flights for the mobile app
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/flights")
@RequiredArgsConstructor
public class MobileFlightController {

    private final FlightService flightService;

    /**
     * Get all active flights
     */
    @GetMapping
    public ResponseEntity<List<Flight>> getAllFlights() {
        log.info("GET /api/mobile/flights");
        List<Flight> flights = flightService.getAllActiveFlights();
        log.info("✅ {} flight(s) found", flights.size());
        return ResponseEntity.ok(flights);
    }

    /**
     * Get flight by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Flight> getFlightById(@PathVariable UUID id) {
        log.info("GET /api/mobile/flights/{}", id);
        Flight flight = flightService.getFlightById(id);
        return ResponseEntity.ok(flight);
    }

    /**
     * Search flights with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlights(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy) {
        log.info("GET /api/mobile/flights/search?origin={}&destination={}&minPrice={}&maxPrice={}",
                origin, destination, minPrice, maxPrice);
        List<Flight> flights = flightService.searchFlights(origin, destination, minPrice, maxPrice, sortBy);
        log.info("✅ {} flight(s) found", flights.size());
        return ResponseEntity.ok(flights);
    }
}
