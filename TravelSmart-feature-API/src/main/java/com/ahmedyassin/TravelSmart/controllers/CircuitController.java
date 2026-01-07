package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.Circuit;
import com.ahmedyassin.TravelSmart.entities.CircuitDay;
import com.ahmedyassin.TravelSmart.entities.CircuitActivity;
import com.ahmedyassin.TravelSmart.services.CircuitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mobile-facing Circuit Controller
 * Provides read-only and search endpoints for the mobile app
 */
@Slf4j
@RestController
@RequestMapping("/api/circuits")
@RequiredArgsConstructor
public class CircuitController {

    private final CircuitService circuitService;

    /**
     * Get all active circuits
     */
    @GetMapping
    public ResponseEntity<List<Circuit>> getAllCircuits() {
        log.info("GET /api/circuits");
        List<Circuit> circuits = circuitService.getAllActiveCircuits();
        log.info("✅ {} circuit(s) found", circuits.size());
        return ResponseEntity.ok(circuits);
    }

    /**
     * Get circuit by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Circuit> getCircuitById(@PathVariable UUID id) {
        log.info("GET /api/circuits/{}", id);
        Circuit circuit = circuitService.getCircuitById(id);
        return ResponseEntity.ok(circuit);
    }

    /**
     * Search circuits with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Circuit>> searchCircuits(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer duration,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy) {
        log.info("GET /api/circuits/search?destination={}&duration={}&minPrice={}&maxPrice={}",
                destination, duration, minPrice, maxPrice);
        List<Circuit> circuits = circuitService.searchCircuits(destination, duration, minPrice, maxPrice, sortBy);
        log.info("✅ {} circuit(s) found", circuits.size());
        return ResponseEntity.ok(circuits);
    }

    /**
     * Get circuit program (daily schedule)
     */
    @GetMapping("/{id}/program")
    public ResponseEntity<List<CircuitDay>> getCircuitProgram(@PathVariable UUID id) {
        log.info("GET /api/circuits/{}/program", id);
        List<CircuitDay> program = circuitService.getCircuitProgram(id);
        log.info("✅ {} day(s) of program", program.size());
        return ResponseEntity.ok(program);
    }

    /**
     * Get circuit optional activities
     */
    @GetMapping("/{id}/activities")
    public ResponseEntity<List<CircuitActivity>> getCircuitActivities(@PathVariable UUID id) {
        log.info("GET /api/circuits/{}/activities", id);
        List<CircuitActivity> activities = circuitService.getCircuitActivities(id);
        log.info("✅ {} activity(ies) found", activities.size());
        return ResponseEntity.ok(activities);
    }

    /**
     * Calculate circuit price based on options
     */
    @PostMapping("/{id}/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {
        log.info("POST /api/circuits/{}/calculate-price", id);
        log.info("Request: {}", request);
        Map<String, Object> breakdown = circuitService.calculatePrice(id, request);
        log.info("✅ Price calculated: {} TND", breakdown.get("totalPrice"));
        return ResponseEntity.ok(breakdown);
    }
}
