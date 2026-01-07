package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.*;
import com.ahmedyassin.TravelSmart.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitService {
    private final CircuitRepository circuitRepository;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;
    private final EtapeRepository etapeRepository;
    private final CircuitDayRepository circuitDayRepository;
    private final CircuitActivityRepository circuitActivityRepository;

    // ===== ADMIN METHODS (Existing) =====

    @Transactional
    public Circuit create(Circuit c) {
        // Handle hotels - fetch from DB to ensure they exist
        if (c.getHotels() != null && !c.getHotels().isEmpty()) {
            List<Hotel> validHotels = new ArrayList<>();
            for (Hotel h : c.getHotels()) {
                if (h.getId() != null) {
                    hotelRepository.findById(h.getId()).ifPresent(validHotels::add);
                }
            }
            c.setHotels(validHotels);
        }
        
        // Handle activities - fetch from DB to ensure they exist
        if (c.getActivities() != null && !c.getActivities().isEmpty()) {
            List<Activity> validActivities = new ArrayList<>();
            for (Activity a : c.getActivities()) {
                if (a.getId() != null) {
                    activityRepository.findById(a.getId()).ifPresent(validActivities::add);
                }
            }
            c.setActivities(validActivities);
        }
        
        // Handle etapes - set circuit reference
        if (c.getEtapes() != null) {
            for (Etape etape : c.getEtapes()) {
                etape.setCircuit(c);
            }
        }
        return circuitRepository.save(c);
    }

    @Transactional
    public Circuit update(UUID id, Circuit updates) {
        Circuit existing = circuitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        
        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        existing.setDuree(updates.getDuree());
        existing.setPrix(updates.getPrix());
        existing.setImageUrl(updates.getImageUrl());
        existing.setDifficulty(updates.getDifficulty());
        existing.setMaxParticipants(updates.getMaxParticipants());
        
        // Update etapes - clear and re-add
        if (updates.getEtapes() != null) {
            existing.getEtapes().clear();
            for (Etape etape : updates.getEtapes()) {
                etape.setCircuit(existing);
                existing.getEtapes().add(etape);
            }
        }
        
        // Update hotels
        if (updates.getHotels() != null) {
            existing.setHotels(updates.getHotels());
        }
        
        // Update activities
        if (updates.getActivities() != null) {
            existing.setActivities(updates.getActivities());
        }
        
        return circuitRepository.save(existing);
    }

    public Optional<Circuit> findById(UUID id) {
        return circuitRepository.findById(id);
    }

    public List<Circuit> findAll() {
        return circuitRepository.findAll();
    }

    @Transactional
    public void delete(UUID id) {
        circuitRepository.deleteById(id);
    }

    // Etape management
    @Transactional
    public Circuit addEtape(UUID circuitId, Etape etape) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        etape.setCircuit(circuit);
        if (etape.getOrdre() == null) {
            etape.setOrdre(circuit.getEtapes().size() + 1);
        }
        circuit.getEtapes().add(etape);
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit updateEtape(UUID circuitId, UUID etapeId, Etape updates) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        
        Etape existing = circuit.getEtapes().stream()
                .filter(e -> e.getId().equals(etapeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Etape not found"));
        
        existing.setOrdre(updates.getOrdre());
        existing.setLieu(updates.getLieu());
        existing.setDescription(updates.getDescription());
        existing.setDureeJours(updates.getDureeJours());
        existing.setHotel(updates.getHotel());
        
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit removeEtape(UUID circuitId, UUID etapeId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        circuit.getEtapes().removeIf(e -> e.getId().equals(etapeId));
        return circuitRepository.save(circuit);
    }

    public List<Etape> getEtapes(UUID circuitId) {
        return etapeRepository.findByCircuitIdOrderByOrdreAsc(circuitId);
    }

    // Hotel association
    @Transactional
    public Circuit addHotel(UUID circuitId, UUID hotelId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));
        if (!circuit.getHotels().contains(hotel)) {
            circuit.getHotels().add(hotel);
        }
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit removeHotel(UUID circuitId, UUID hotelId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        circuit.getHotels().removeIf(h -> h.getId().equals(hotelId));
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit setHotels(UUID circuitId, List<UUID> hotelIds) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        List<Hotel> hotels = hotelRepository.findAllById(hotelIds);
        circuit.setHotels(hotels);
        return circuitRepository.save(circuit);
    }

    // Activity association
    @Transactional
    public Circuit addActivity(UUID circuitId, UUID activityId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("Activity not found"));
        if (!circuit.getActivities().contains(activity)) {
            circuit.getActivities().add(activity);
        }
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit removeActivity(UUID circuitId, UUID activityId) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        circuit.getActivities().removeIf(a -> a.getId().equals(activityId));
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit setActivities(UUID circuitId, List<UUID> activityIds) {
        Circuit circuit = circuitRepository.findById(circuitId)
                .orElseThrow(() -> new RuntimeException("Circuit not found"));
        List<Activity> activities = activityRepository.findAllById(activityIds);
        circuit.setActivities(activities);
        return circuitRepository.save(circuit);
    }

    // ===== MOBILE METHODS (New) =====

    /**
     * Get all active circuits for mobile app
     */
    public List<Circuit> getAllActiveCircuits() {
        return circuitRepository.findByIsActiveTrue();
    }

    /**
     * Get circuit by ID (for mobile)
     */
    public Circuit getCircuitById(UUID id) {
        return circuitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Circuit not found: " + id));
    }

    /**
     * Search circuits with filters (for mobile)
     */
    public List<Circuit> searchCircuits(String destination, Integer duration, Double minPrice, Double maxPrice, String sortBy) {
        log.info("=== CIRCUIT SEARCH ===");
        log.info("Destination: {}", destination);
        log.info("Duration: {}", duration);
        log.info("Price range: {} - {}", minPrice, maxPrice);

        List<Circuit> circuits = circuitRepository.findByIsActiveTrue();
        log.info("Active circuits found: {}", circuits.size());

        // Filter by destination
        if (destination != null && !destination.isEmpty()) {
            String destLower = destination.toLowerCase();
            circuits = circuits.stream()
                    .filter(c -> {
                        if (c.getDestinations() != null && !c.getDestinations().isEmpty()) {
                            return c.getDestinations().stream()
                                    .anyMatch(d -> d.toLowerCase().contains(destLower));
                        }
                        return false;
                    })
                    .toList();
            log.info("After destination filter: {}", circuits.size());
        }

        // Filter by duration
        if (duration != null) {
            circuits = circuits.stream()
                    .filter(c -> c.getDuree() != null && c.getDuree().equals(duration))
                    .toList();
            log.info("After duration filter: {}", circuits.size());
        }

        // Filter by min price
        if (minPrice != null) {
            circuits = circuits.stream()
                    .filter(c -> c.getPrix() >= minPrice)
                    .toList();
        }

        // Filter by max price
        if (maxPrice != null) {
            circuits = circuits.stream()
                    .filter(c -> c.getPrix() <= maxPrice)
                    .toList();
        }

        // Sort by price
        if ("price".equals(sortBy)) {
            circuits = circuits.stream()
                    .sorted((c1, c2) -> Double.compare(c1.getPrix(), c2.getPrix()))
                    .toList();
        }

        log.info("=== RESULT: {} circuits ===", circuits.size());
        return circuits;
    }

    /**
     * Get circuit program (daily schedule) for mobile
     */
    public List<CircuitDay> getCircuitProgram(UUID circuitId) {
        log.info("Getting program for circuit: {}", circuitId);
        return circuitDayRepository.findByCircuitIdOrderByDayNumberAsc(circuitId);
    }

    /**
     * Get circuit activities for mobile
     */
    public List<CircuitActivity> getCircuitActivities(UUID circuitId) {
        log.info("Getting activities for circuit: {}", circuitId);
        return circuitActivityRepository.findByCircuitId(circuitId);
    }

    /**
     * Calculate circuit price for mobile
     */
    public Map<String, Object> calculatePrice(UUID circuitId, Map<String, Object> request) {
        Circuit circuit = getCircuitById(circuitId);

        int adults = (int) request.getOrDefault("adults", 1);
        @SuppressWarnings("unchecked")
        List<Integer> children = (List<Integer>) request.getOrDefault("children", List.of());
        String hotelLevel = (String) request.getOrDefault("hotelLevel", "STANDARD");
        String flightClass = (String) request.getOrDefault("flightClass", "ECONOMY");
        @SuppressWarnings("unchecked")
        List<String> selectedActivityIds = (List<String>) request.getOrDefault("selectedActivities", List.of());

        double basePrice = circuit.getPrix();

        // 1. Adults price
        double adultsPrice = adults * basePrice;

        // 2. Children price (0-4 free, 5-18 = 70%)
        int freeChildren = (int) children.stream().filter(age -> age <= 4).count();
        double childrenPrice = children.stream()
                .filter(age -> age > 4)
                .mapToDouble(age -> age <= 18 ? basePrice * 0.7 : basePrice)
                .sum();

        // 3. Hotel supplement
        int payingPersons = adults + (int) children.stream().filter(age -> age > 4).count();
        double hotelExtra = getHotelExtra(hotelLevel) * payingPersons;

        // 4. Flight supplement
        double flightExtra = getFlightExtra(flightClass) * payingPersons;

        // 5. Optional activities
        double activitiesPrice = selectedActivityIds.stream()
                .map(id -> {
                    try {
                        return circuitActivityRepository.findById(UUID.fromString(id))
                                .map(CircuitActivity::getPrice)
                                .orElse(0.0);
                    } catch (Exception e) {
                        return 0.0;
                    }
                })
                .mapToDouble(Double::doubleValue)
                .sum();

        // Total
        double totalPrice = adultsPrice + childrenPrice + hotelExtra + flightExtra + activitiesPrice;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("basePrice", basePrice);
        breakdown.put("adults", adults);
        breakdown.put("adultsPrice", adultsPrice);
        breakdown.put("children", children);
        breakdown.put("childrenPrice", childrenPrice);
        breakdown.put("freeChildren", freeChildren);
        breakdown.put("hotelLevel", hotelLevel);
        breakdown.put("hotelExtra", hotelExtra);
        breakdown.put("flightClass", flightClass);
        breakdown.put("flightExtra", flightExtra);
        breakdown.put("activitiesPrice", activitiesPrice);
        breakdown.put("totalPrice", totalPrice);

        return breakdown;
    }

    private double getHotelExtra(String level) {
        return switch (level) {
            case "SUPERIOR" -> 200.0;
            case "LUXURY" -> 450.0;
            default -> 0.0;
        };
    }

    private double getFlightExtra(String flightClass) {
        return switch (flightClass) {
            case "BUSINESS" -> 400.0;
            case "FIRST" -> 900.0;
            default -> 0.0;
        };
    }
}

