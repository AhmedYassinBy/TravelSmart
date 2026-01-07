package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Flight;
import com.ahmedyassin.TravelSmart.repositories.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightRepository flightRepository;

    // ===== ADMIN METHODS (Existing) =====

    @Transactional
    public Flight create(Flight f){
        return flightRepository.save(f);
    }

    @Transactional
    public Flight update(UUID id, Flight updates){
        Flight existing = flightRepository.findById(id).orElseThrow(() -> new RuntimeException("Flight not found"));
        existing.setAirline(updates.getAirline());
        existing.setFlightNumber(updates.getFlightNumber());
        existing.setOrigin(updates.getOrigin());
        existing.setDestination(updates.getDestination());
        existing.setDepartureTime(updates.getDepartureTime());
        existing.setArrivalTime(updates.getArrivalTime());
        existing.setPrice(updates.getPrice());
        existing.setSeatsAvailable(updates.getSeatsAvailable());
        // Update new fields
        if (updates.getClassType() != null) existing.setClassType(updates.getClassType());
        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
        return flightRepository.save(existing);
    }

    public Optional<Flight> findById(UUID id){
        return flightRepository.findById(id);
    }

    public List<Flight> findAll(){
        return flightRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        flightRepository.deleteById(id);
    }

    // ===== MOBILE METHODS (New) =====

    /**
     * Get all active flights for mobile app
     */
    public List<Flight> getAllActiveFlights() {
        return flightRepository.findByIsActiveTrue();
    }

    /**
     * Get flight by ID (for mobile)
     */
    public Flight getFlightById(UUID id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + id));
    }

    /**
     * Search flights with filters (for mobile)
     */
    public List<Flight> searchFlights(String origin, String destination, Double minPrice, Double maxPrice, String sortBy) {
        log.info("=== FLIGHT SEARCH ===");
        log.info("Origin: {}, Destination: {}", origin, destination);
        log.info("Price range: {} - {}", minPrice, maxPrice);

        List<Flight> flights = flightRepository.findByIsActiveTrue();

        // Filter by origin
        if (origin != null && !origin.isEmpty()) {
            flights = flights.stream()
                    .filter(f -> f.getOrigin() != null && f.getOrigin().toLowerCase().contains(origin.toLowerCase()))
                    .toList();
        }

        // Filter by destination
        if (destination != null && !destination.isEmpty()) {
            flights = flights.stream()
                    .filter(f -> f.getDestination() != null && f.getDestination().toLowerCase().contains(destination.toLowerCase()))
                    .toList();
        }

        // Filter by min price
        if (minPrice != null) {
            flights = flights.stream()
                    .filter(f -> f.getPrice() != null && f.getPrice() >= minPrice)
                    .toList();
        }

        // Filter by max price
        if (maxPrice != null) {
            flights = flights.stream()
                    .filter(f -> f.getPrice() != null && f.getPrice() <= maxPrice)
                    .toList();
        }

        // Sort by price
        if ("price".equals(sortBy)) {
            flights = flights.stream()
                    .sorted((f1, f2) -> {
                        Double p1 = f1.getPrice() != null ? f1.getPrice() : 0.0;
                        Double p2 = f2.getPrice() != null ? f2.getPrice() : 0.0;
                        return Double.compare(p1, p2);
                    })
                    .toList();
        }

        log.info("=== RESULT: {} flights ===", flights.size());
        return flights;
    }
}

