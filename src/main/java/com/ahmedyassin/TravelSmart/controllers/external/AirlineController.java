package com.ahmedyassin.TravelSmart.controllers.external;

import com.ahmedyassin.TravelSmart.dto.external.AirlineDTO;
import com.ahmedyassin.TravelSmart.services.external.AirlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airlines")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AirlineController {

    private final AirlineService airlineService;

    /**
     * Rechercher des compagnies aériennes
     * Example: GET /api/airlines/search?airlineName=Air France
     */
    @GetMapping("/search")
    public ResponseEntity<List<AirlineDTO>> searchAirlines(
            @RequestParam(required = false) String airlineName) {
        log.info("REST Request to search airlines with name: {}", airlineName);
        List<AirlineDTO> airlines = airlineService.searchAirlines(airlineName);
        return ResponseEntity.ok(airlines);
    }
}

