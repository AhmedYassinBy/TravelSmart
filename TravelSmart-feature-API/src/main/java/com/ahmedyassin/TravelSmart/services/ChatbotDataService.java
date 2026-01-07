package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.*;
import com.ahmedyassin.TravelSmart.enums.ReservationStatus;
import com.ahmedyassin.TravelSmart.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that provides database access for the AI Chatbot.
 * This service aggregates data from various repositories and formats it
 * for the AI assistant to understand and present to users.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotDataService {

    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final CircuitRepository circuitRepository;
    private final ClientRepository clientRepository;

    // ==================== STATISTICS ====================

    /**
     * Get comprehensive dashboard statistics
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Flight stats
        long totalFlights = flightRepository.count();
        long activeFlights = flightRepository.findByIsActiveTrue().size();
        stats.put("totalFlights", totalFlights);
        stats.put("activeFlights", activeFlights);

        // Hotel stats
        long totalHotels = hotelRepository.count();
        long activeHotels = hotelRepository.findByIsActiveTrue().size();
        stats.put("totalHotels", totalHotels);
        stats.put("activeHotels", activeHotels);

        // Room stats
        long totalRooms = roomRepository.count();
        stats.put("totalRooms", totalRooms);

        // Reservation stats
        long totalReservations = reservationRepository.count();
        long pendingReservations = reservationRepository.findByStatus(ReservationStatus.PENDING).size();
        long confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED).size();
        long cancelledReservations = reservationRepository.findByStatus(ReservationStatus.CANCELLED).size();
        stats.put("totalReservations", totalReservations);
        stats.put("pendingReservations", pendingReservations);
        stats.put("confirmedReservations", confirmedReservations);
        stats.put("cancelledReservations", cancelledReservations);

        // Circuit stats
        long totalCircuits = circuitRepository.count();
        stats.put("totalCircuits", totalCircuits);

        // Client stats
        long totalClients = clientRepository.count();
        stats.put("totalClients", totalClients);

        return stats;
    }

    // ==================== FLIGHTS ====================

    /**
     * Get flight count and summary
     */
    public String getFlightsSummary() {
        List<Flight> flights = flightRepository.findAll();
        long totalFlights = flights.size();
        long activeFlights = flights.stream().filter(f -> Boolean.TRUE.equals(f.getIsActive())).count();

        if (totalFlights == 0) {
            return "There are currently no flights in the database.";
        }

        // Group by airline
        Map<String, Long> byAirline = flights.stream()
                .filter(f -> f.getAirline() != null)
                .collect(Collectors.groupingBy(Flight::getAirline, Collectors.counting()));

        // Group by destination
        Map<String, Long> byDestination = flights.stream()
                .filter(f -> f.getDestination() != null)
                .collect(Collectors.groupingBy(Flight::getDestination, Collectors.counting()));

        // Price range
        DoubleSummaryStatistics priceStats = flights.stream()
                .filter(f -> f.getPrice() != null)
                .mapToDouble(Flight::getPrice)
                .summaryStatistics();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Flight Statistics:**\n"));
        sb.append(String.format("- Total flights: **%d**\n", totalFlights));
        sb.append(String.format("- Active flights: **%d**\n", activeFlights));
        sb.append(String.format("- Inactive flights: **%d**\n", totalFlights - activeFlights));

        if (!byAirline.isEmpty()) {
            sb.append("\n✈️ **Flights by Airline:**\n");
            byAirline.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append(String.format("- %s: %d flights\n", e.getKey(), e.getValue())));
        }

        if (!byDestination.isEmpty()) {
            sb.append("\n🌍 **Top Destinations:**\n");
            byDestination.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append(String.format("- %s: %d flights\n", e.getKey(), e.getValue())));
        }

        if (priceStats.getCount() > 0) {
            sb.append(String.format("\n💰 **Price Range:** $%.2f - $%.2f (Avg: $%.2f)\n",
                    priceStats.getMin(), priceStats.getMax(), priceStats.getAverage()));
        }

        return sb.toString();
    }

    /**
     * Get list of all flights with details
     */
    public String getFlightsList(int limit) {
        List<Flight> flights = flightRepository.findAll();

        if (flights.isEmpty()) {
            return "No flights found in the database.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 **Flight List** (showing %d of %d):\n\n",
                Math.min(limit, flights.size()), flights.size()));

        flights.stream().limit(limit).forEach(f -> {
            sb.append(String.format("**%s** - %s\n",
                    f.getFlightNumber() != null ? f.getFlightNumber() : "N/A",
                    f.getAirline() != null ? f.getAirline() : "Unknown Airline"));
            sb.append(String.format("  Route: %s → %s\n",
                    f.getOrigin() != null ? f.getOrigin() : "N/A",
                    f.getDestination() != null ? f.getDestination() : "N/A"));
            if (f.getDepartureTime() != null) {
                sb.append(String.format("  Departure: %s\n", f.getDepartureTime()));
            }
            if (f.getPrice() != null) {
                sb.append(String.format("  Price: $%.2f\n", f.getPrice()));
            }
            sb.append(String.format("  Status: %s\n\n",
                    Boolean.TRUE.equals(f.getIsActive()) ? "✅ Active" : "❌ Inactive"));
        });

        return sb.toString();
    }

    /**
     * Search flights by criteria
     */
    public String searchFlights(String origin, String destination) {
        List<Flight> flights = flightRepository.findAll();

        List<Flight> filtered = flights.stream()
                .filter(f -> (origin == null || origin.isEmpty() ||
                        (f.getOrigin() != null && f.getOrigin().toLowerCase().contains(origin.toLowerCase()))))
                .filter(f -> (destination == null || destination.isEmpty() ||
                        (f.getDestination() != null
                                && f.getDestination().toLowerCase().contains(destination.toLowerCase()))))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return String.format("No flights found from '%s' to '%s'.",
                    origin != null ? origin : "any", destination != null ? destination : "any");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🔍 Found **%d flights** ", filtered.size()));
        if (origin != null && !origin.isEmpty())
            sb.append(String.format("from '%s' ", origin));
        if (destination != null && !destination.isEmpty())
            sb.append(String.format("to '%s'", destination));
        sb.append(":\n\n");

        filtered.stream().limit(10).forEach(f -> {
            sb.append(String.format("• **%s** (%s): %s → %s | $%.2f | %s\n",
                    f.getFlightNumber() != null ? f.getFlightNumber() : "N/A",
                    f.getAirline() != null ? f.getAirline() : "N/A",
                    f.getOrigin(),
                    f.getDestination(),
                    f.getPrice() != null ? f.getPrice() : 0,
                    Boolean.TRUE.equals(f.getIsActive()) ? "Active" : "Inactive"));
        });

        return sb.toString();
    }

    // ==================== HOTELS ====================

    /**
     * Get hotel count and summary
     */
    public String getHotelsSummary() {
        List<Hotel> hotels = hotelRepository.findAll();
        long totalHotels = hotels.size();
        long activeHotels = hotels.stream().filter(h -> Boolean.TRUE.equals(h.getIsActive())).count();

        if (totalHotels == 0) {
            return "There are currently no hotels in the database.";
        }

        // Group by city
        Map<String, Long> byCity = hotels.stream()
                .filter(h -> h.getCity() != null)
                .collect(Collectors.groupingBy(Hotel::getCity, Collectors.counting()));

        // Group by star rating
        Map<Integer, Long> byStars = hotels.stream()
                .filter(h -> h.getEtoile() != null)
                .collect(Collectors.groupingBy(Hotel::getEtoile, Collectors.counting()));

        // Price range
        DoubleSummaryStatistics priceStats = hotels.stream()
                .filter(h -> h.getPricePerNight() != null)
                .mapToDouble(Hotel::getPricePerNight)
                .summaryStatistics();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Hotel Statistics:**\n"));
        sb.append(String.format("- Total hotels: **%d**\n", totalHotels));
        sb.append(String.format("- Active hotels: **%d**\n", activeHotels));
        sb.append(String.format("- Inactive hotels: **%d**\n", totalHotels - activeHotels));

        // Count total rooms
        long totalRooms = hotels.stream()
                .mapToLong(h -> h.getRooms() != null ? h.getRooms().size() : 0)
                .sum();
        sb.append(String.format("- Total rooms: **%d**\n", totalRooms));

        if (!byCity.isEmpty()) {
            sb.append("\n🏙️ **Hotels by City:**\n");
            byCity.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(e -> sb.append(String.format("- %s: %d hotels\n", e.getKey(), e.getValue())));
        }

        if (!byStars.isEmpty()) {
            sb.append("\n⭐ **Hotels by Star Rating:**\n");
            byStars.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByKey().reversed())
                    .forEach(e -> sb.append(String.format("- %d stars: %d hotels\n", e.getKey(), e.getValue())));
        }

        if (priceStats.getCount() > 0) {
            sb.append(String.format("\n💰 **Price Range (per night):** $%.2f - $%.2f (Avg: $%.2f)\n",
                    priceStats.getMin(), priceStats.getMax(), priceStats.getAverage()));
        }

        return sb.toString();
    }

    /**
     * Get list of all hotels with details
     */
    public String getHotelsList(int limit) {
        List<Hotel> hotels = hotelRepository.findAll();

        if (hotels.isEmpty()) {
            return "No hotels found in the database.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 **Hotel List** (showing %d of %d):\n\n",
                Math.min(limit, hotels.size()), hotels.size()));

        hotels.stream().limit(limit).forEach(h -> {
            sb.append(String.format("**%s** ", h.getName() != null ? h.getName() : "Unnamed Hotel"));
            if (h.getEtoile() != null) {
                sb.append("⭐".repeat(h.getEtoile()));
            }
            sb.append("\n");
            sb.append(String.format("  📍 %s, %s\n",
                    h.getCity() != null ? h.getCity() : "N/A",
                    h.getCountry() != null ? h.getCountry() : "N/A"));
            if (h.getPricePerNight() != null) {
                sb.append(String.format("  💰 $%.2f/night\n", h.getPricePerNight()));
            }
            int roomCount = h.getRooms() != null ? h.getRooms().size() : 0;
            sb.append(String.format("  🛏️ %d rooms\n", roomCount));
            sb.append(String.format("  Status: %s\n\n",
                    Boolean.TRUE.equals(h.getIsActive()) ? "✅ Active" : "❌ Inactive"));
        });

        return sb.toString();
    }

    /**
     * Search hotels by city
     */
    public String searchHotels(String city, Integer minStars) {
        List<Hotel> hotels = hotelRepository.findAll();

        List<Hotel> filtered = hotels.stream()
                .filter(h -> (city == null || city.isEmpty() ||
                        (h.getCity() != null && h.getCity().toLowerCase().contains(city.toLowerCase()))))
                .filter(h -> (minStars == null ||
                        (h.getEtoile() != null && h.getEtoile() >= minStars)))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return String.format("No hotels found in '%s'%s.",
                    city != null ? city : "any city",
                    minStars != null ? " with " + minStars + "+ stars" : "");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🔍 Found **%d hotels**", filtered.size()));
        if (city != null && !city.isEmpty())
            sb.append(String.format(" in '%s'", city));
        if (minStars != null)
            sb.append(String.format(" with %d+ stars", minStars));
        sb.append(":\n\n");

        filtered.stream().limit(10).forEach(h -> {
            sb.append(String.format("• **%s** ", h.getName()));
            if (h.getEtoile() != null)
                sb.append("⭐".repeat(h.getEtoile()));
            sb.append(String.format(" | %s | $%.2f/night | %s\n",
                    h.getCity() != null ? h.getCity() : "N/A",
                    h.getPricePerNight() != null ? h.getPricePerNight() : 0,
                    Boolean.TRUE.equals(h.getIsActive()) ? "Active" : "Inactive"));
        });

        return sb.toString();
    }

    // ==================== RESERVATIONS ====================

    /**
     * Get reservations summary
     */
    public String getReservationsSummary() {
        List<Reservation> reservations = reservationRepository.findAll();
        long total = reservations.size();

        if (total == 0) {
            return "There are currently no reservations in the database.";
        }

        Map<ReservationStatus, Long> byStatus = reservations.stream()
                .filter(r -> r.getStatus() != null)
                .collect(Collectors.groupingBy(Reservation::getStatus, Collectors.counting()));

        Map<String, Long> byType = reservations.stream()
                .filter(r -> r.getOfferType() != null)
                .collect(Collectors.groupingBy(Reservation::getOfferType, Collectors.counting()));

        // Calculate revenue
        double totalRevenue = reservations.stream()
                .filter(r -> r.getPrice() != null && r.getStatus() == ReservationStatus.CONFIRMED)
                .mapToDouble(Reservation::getPrice)
                .sum();

        // Recent reservations (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentCount = reservations.stream()
                .filter(r -> r.getBookingDate() != null && r.getBookingDate().isAfter(weekAgo))
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Reservation Statistics:**\n"));
        sb.append(String.format("- Total reservations: **%d**\n", total));
        sb.append(String.format("- Reservations this week: **%d**\n", recentCount));
        sb.append(String.format("- Total confirmed revenue: **$%.2f**\n", totalRevenue));

        sb.append("\n📈 **By Status:**\n");
        byStatus.forEach((status, count) -> sb.append(String.format("- %s: %d\n", status, count)));

        if (!byType.isEmpty()) {
            sb.append("\n🏷️ **By Type:**\n");
            byType.forEach((type, count) -> sb.append(String.format("- %s: %d\n", type, count)));
        }

        return sb.toString();
    }

    /**
     * Get recent reservations
     */
    public String getRecentReservations(int limit) {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            return "No reservations found in the database.";
        }

        // Sort by booking date descending
        List<Reservation> sorted = reservations.stream()
                .sorted((a, b) -> {
                    if (a.getBookingDate() == null)
                        return 1;
                    if (b.getBookingDate() == null)
                        return -1;
                    return b.getBookingDate().compareTo(a.getBookingDate());
                })
                .limit(limit)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 **Recent Reservations** (showing %d of %d):\n\n",
                sorted.size(), reservations.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        sorted.forEach(r -> {
            sb.append(String.format("**%s** - %s\n",
                    r.getOfferType() != null ? r.getOfferType() : "N/A",
                    r.getOfferName() != null ? r.getOfferName() : "N/A"));
            sb.append(String.format("  📧 %s\n", r.getClientEmail() != null ? r.getClientEmail() : "N/A"));
            sb.append(String.format("  💰 $%.2f\n", r.getPrice() != null ? r.getPrice() : 0));
            sb.append(String.format("  📅 %s\n",
                    r.getBookingDate() != null ? r.getBookingDate().format(formatter) : "N/A"));
            sb.append(String.format("  Status: %s\n\n", getStatusEmoji(r.getStatus())));
        });

        return sb.toString();
    }

    /**
     * Get pending reservations that need attention
     */
    public String getPendingReservations() {
        List<Reservation> pending = reservationRepository.findByStatus(ReservationStatus.PENDING);

        if (pending.isEmpty()) {
            return "✅ Great news! There are no pending reservations that need attention.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("⚠️ **Pending Reservations** (%d total):\n\n", pending.size()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        pending.stream().limit(10).forEach(r -> {
            sb.append(String.format("• **%s** - %s | %s | $%.2f | %s\n",
                    r.getOfferType() != null ? r.getOfferType() : "N/A",
                    r.getOfferName() != null ? r.getOfferName() : "N/A",
                    r.getClientEmail() != null ? r.getClientEmail() : "N/A",
                    r.getPrice() != null ? r.getPrice() : 0,
                    r.getBookingDate() != null ? r.getBookingDate().format(formatter) : "N/A"));
        });

        if (pending.size() > 10) {
            sb.append(String.format("\n... and %d more pending reservations.", pending.size() - 10));
        }

        return sb.toString();
    }

    // ==================== CIRCUITS ====================

    /**
     * Get circuits summary
     */
    public String getCircuitsSummary() {
        List<Circuit> circuits = circuitRepository.findAll();
        long total = circuits.size();

        if (total == 0) {
            return "There are currently no circuits/tours in the database.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Circuit/Tour Statistics:**\n"));
        sb.append(String.format("- Total circuits: **%d**\n", total));

        // Price stats
        DoubleSummaryStatistics priceStats = circuits.stream()
                .filter(c -> c.getPrix() != null)
                .mapToDouble(Circuit::getPrix)
                .summaryStatistics();

        if (priceStats.getCount() > 0) {
            sb.append(String.format("- Price range: $%.2f - $%.2f\n",
                    priceStats.getMin(), priceStats.getMax()));
            sb.append(String.format("- Average price: $%.2f\n", priceStats.getAverage()));
        }

        return sb.toString();
    }

    // ==================== CLIENTS ====================

    /**
     * Get clients summary
     */
    public String getClientsSummary() {
        long totalClients = clientRepository.count();

        if (totalClients == 0) {
            return "There are currently no registered clients.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 **Client Statistics:**\n"));
        sb.append(String.format("- Total registered clients: **%d**\n", totalClients));

        return sb.toString();
    }

    // ==================== HELPER METHODS ====================

    private String getStatusEmoji(ReservationStatus status) {
        if (status == null)
            return "❓ Unknown";
        return switch (status) {
            case PENDING -> "🟡 Pending";
            case CONFIRMED -> "✅ Confirmed";
            case CANCELLED -> "❌ Cancelled";
            case COMPLETED -> "✔️ Completed";
            default -> throw new IllegalArgumentException("Unexpected value: " + status);
        };
    }

    /**
     * Build a comprehensive context string for the AI
     */
    public String buildDatabaseContext() {
        Map<String, Object> stats = getDashboardStats();

        StringBuilder context = new StringBuilder();
        context.append("=== CURRENT DATABASE STATE ===\n\n");

        context.append(String.format("FLIGHTS: %d total (%d active)\n",
                stats.get("totalFlights"), stats.get("activeFlights")));
        context.append(String.format("HOTELS: %d total (%d active), %d rooms\n",
                stats.get("totalHotels"), stats.get("activeHotels"), stats.get("totalRooms")));
        context.append(String.format("RESERVATIONS: %d total (Pending: %d, Confirmed: %d, Cancelled: %d)\n",
                stats.get("totalReservations"), stats.get("pendingReservations"),
                stats.get("confirmedReservations"), stats.get("cancelledReservations")));
        context.append(String.format("CIRCUITS: %d total\n", stats.get("totalCircuits")));
        context.append(String.format("CLIENTS: %d registered\n", stats.get("totalClients")));

        return context.toString();
    }
}
