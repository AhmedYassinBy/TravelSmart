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
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final HotelRepository hotelRepository;
    private final FlightRepository flightRepository;
    private final CircuitRepository circuitRepository;
    private final RoomRepository roomRepository;
    private final EmailService emailService;

    /**
     * Get all reservations for a client
     */
    public List<Reservation> getReservationsByEmail(String email) {
        log.info("Fetching reservations for: {}", email);
        return reservationRepository.findByClientEmailOrderByBookingDateDesc(email);
    }

    /**
     * Get reservation details
     */
    public Map<String, Object> getReservationDetails(UUID reservationId) {
        log.info("Fetching reservation details: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        Map<String, Object> details = new HashMap<>();
        details.put("id", reservation.getId().toString());
        details.put("clientEmail", reservation.getClientEmail());
        details.put("offerType", reservation.getOfferType());
        details.put("offerName", reservation.getOfferName());
        details.put("price", reservation.getPrice());
        details.put("status", reservation.getStatus().toString());
        details.put("bookingDate", reservation.getBookingDate().toString());
        details.put("paymentMethod", reservation.getPaymentMethod());

        if (reservation.getStartDate() != null) {
            details.put("startDate", reservation.getStartDate().toString());
        }
        if (reservation.getEndDate() != null) {
            details.put("endDate", reservation.getEndDate().toString());
        }
        if (reservation.getCheckIn() != null) {
            details.put("checkIn", reservation.getCheckIn().toString());
        }
        if (reservation.getCheckOut() != null) {
            details.put("checkOut", reservation.getCheckOut().toString());
        }
        if (reservation.getAdultsCount() != null) {
            details.put("adultsCount", reservation.getAdultsCount());
        }
        if (reservation.getChildrenCount() != null) {
            details.put("childrenCount", reservation.getChildrenCount());
        }
        if (reservation.getFormula() != null) {
            details.put("formula", reservation.getFormula());
        }
        if (reservation.getChildrenAges() != null) {
            details.put("childrenAges", reservation.getChildrenAges());
        }
        if (reservation.getHotelLevel() != null) {
            details.put("hotelLevel", reservation.getHotelLevel());
        }
        if (reservation.getFlightClass() != null) {
            details.put("flightClass", reservation.getFlightClass());
        }
        if (reservation.getSelectedActivities() != null) {
            details.put("selectedActivities", reservation.getSelectedActivities());
        }
        if (reservation.getPriceBreakdown() != null) {
            details.put("priceBreakdown", reservation.getPriceBreakdown());
        }

        return details;
    }

    /**
     * Create a new reservation
     */
    public Map<String, Object> createReservation(
            String email,
            UUID offerId,
            String offerType,
            Double price,
            String paymentMethod,
            String roomType,
            String checkInDate,
            String checkOutDate,
            Integer adultsCount,
            Integer childrenCount,
            String formula,
            String childrenAges,
            String hotelLevel,
            String flightClass,
            String selectedActivities,
            String priceBreakdown) {

        log.info("Creating reservation {} for {}", offerType, email);

        Reservation reservation = new Reservation();
        reservation.setClientEmail(email);
        reservation.setOfferId(offerId);
        reservation.setOfferType(offerType);
        reservation.setPrice(price);
        reservation.setPaymentMethod(paymentMethod);
        reservation.setBookingDate(LocalDateTime.now());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setAdultsCount(adultsCount);
        reservation.setChildrenCount(childrenCount);
        reservation.setFormula(formula);

        // Store circuit-specific details
        if (childrenAges != null && !childrenAges.isEmpty()) {
            reservation.setChildrenAges(childrenAges);
        }
        if (hotelLevel != null && !hotelLevel.isEmpty()) {
            reservation.setHotelLevel(hotelLevel);
        }
        if (flightClass != null && !flightClass.isEmpty()) {
            reservation.setFlightClass(flightClass);
        }
        if (selectedActivities != null && !selectedActivities.isEmpty()) {
            reservation.setSelectedActivities(selectedActivities);
        }
        if (priceBreakdown != null && !priceBreakdown.isEmpty()) {
            reservation.setPriceBreakdown(priceBreakdown);
        }

        String offerName = "";

        try {
            switch (offerType.toLowerCase()) {
                case "hotel":
                    Hotel hotel = hotelRepository.findById(offerId)
                            .orElseThrow(() -> new RuntimeException("Hotel not found"));
                    offerName = hotel.getName();

                    // Find room_id if roomType is provided
                    if (roomType != null && !roomType.isEmpty()) {
                        Optional<Room> room = roomRepository.findByHotelIdAndRoomType(offerId, roomType);
                        room.ifPresent(r -> reservation.setRoomId(r.getId()));
                    }

                    // Parse dates for hotels
                    parseDates(reservation, checkInDate, checkOutDate, true);
                    break;

                case "flight":
                    Flight flight = flightRepository.findById(offerId)
                            .orElseThrow(() -> new RuntimeException("Flight not found"));
                    offerName = flight.getAirline() + " - " + flight.getOrigin() + " → " + flight.getDestination();

                    // Handle flight dates
                    if (checkInDate != null && !checkInDate.isEmpty() && flight.getDepartureTime() != null) {
                        LocalDate userDate = parseDate(checkInDate);
                        if (userDate != null) {
                            java.time.LocalTime departureTimeOnly = flight.getDepartureTime().toLocalTime();
                            LocalDateTime userDepartureDateTime = LocalDateTime.of(userDate, departureTimeOnly);

                            long durationMinutes = java.time.Duration.between(
                                    flight.getDepartureTime(),
                                    flight.getArrivalTime()
                            ).toMinutes();

                            LocalDateTime userArrivalDateTime = userDepartureDateTime.plusMinutes(durationMinutes);

                            reservation.setStartDate(userDepartureDateTime);
                            reservation.setEndDate(userArrivalDateTime);
                        } else {
                            reservation.setStartDate(flight.getDepartureTime());
                            reservation.setEndDate(flight.getArrivalTime());
                        }
                    } else {
                        reservation.setStartDate(flight.getDepartureTime());
                        reservation.setEndDate(flight.getArrivalTime());
                    }
                    break;

                case "circuit":
                    Circuit circuit = circuitRepository.findById(offerId)
                            .orElseThrow(() -> new RuntimeException("Circuit not found"));
                    offerName = circuit.getTitle();
                    parseDates(reservation, checkInDate, checkOutDate, false);
                    break;

                default:
                    throw new RuntimeException("Unknown offer type: " + offerType);
            }
        } catch (Exception e) {
            log.error("Error getting offer name: {}", e.getMessage());
        }

        reservation.setOfferName(offerName);
        Reservation saved = reservationRepository.save(reservation);

        // Send confirmation email
        try {
            emailService.sendBookingConfirmation(email, offerName, saved.getId().toString(), price);
        } catch (Exception e) {
            log.warn("Failed to send booking confirmation email: {}", e.getMessage());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reservationId", saved.getId().toString());
        response.put("message", "Reservation created successfully");
        response.put("offerName", offerName);

        return response;
    }

    /**
     * Cancel a reservation
     */
    public Map<String, Object> cancelReservation(UUID reservationId) {
        log.info("Cancelling reservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        Map<String, Object> result = new HashMap<>();

        // Check if already cancelled
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            result.put("success", false);
            result.put("error", "Reservation is already cancelled");
            return result;
        }

        // Calculate refund based on cancellation policy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = reservation.getStartDate();
        double refundPercentage = 100.0;
        String penaltyMessage = "";

        if (startDate != null) {
            long hoursUntilStart = ChronoUnit.HOURS.between(now, startDate);
            long daysUntilStart = ChronoUnit.DAYS.between(now, startDate);

            if (hoursUntilStart < 24) {
                refundPercentage = 0.0;
                penaltyMessage = "Cancellation within 24 hours: No refund";
            } else if (daysUntilStart < 3) {
                refundPercentage = 50.0;
                penaltyMessage = "Cancellation within 3 days: 50% refund";
            } else if (daysUntilStart < 7) {
                refundPercentage = 75.0;
                penaltyMessage = "Cancellation within 7 days: 75% refund";
            } else {
                refundPercentage = 100.0;
                penaltyMessage = "Full refund";
            }

            result.put("daysUntilStart", daysUntilStart);
            result.put("hoursUntilStart", hoursUntilStart);
        }

        double refundAmount = reservation.getPrice() * (refundPercentage / 100.0);
        double penaltyAmount = reservation.getPrice() - refundAmount;

        // Update reservation status
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        result.put("success", true);
        result.put("reservationId", reservationId.toString());
        result.put("offerType", reservation.getOfferType());
        result.put("offerName", reservation.getOfferName());
        result.put("originalPrice", reservation.getPrice());
        result.put("refundPercentage", refundPercentage);
        result.put("refundAmount", refundAmount);
        result.put("penaltyAmount", penaltyAmount);
        result.put("penaltyMessage", penaltyMessage);
        result.put("message", "Reservation cancelled successfully");

        return result;
    }

    // Helper methods

    private void parseDates(Reservation reservation, String checkInDate, String checkOutDate, boolean isHotel) {
        if (checkInDate != null && !checkInDate.isEmpty()) {
            LocalDate date = parseDate(checkInDate);
            if (date != null) {
                if (isHotel) {
                    reservation.setCheckIn(date.atTime(12, 0));
                }
                reservation.setStartDate(date.atTime(isHotel ? 12 : 8, 0));
            }
        }

        if (checkOutDate != null && !checkOutDate.isEmpty()) {
            LocalDate date = parseDate(checkOutDate);
            if (date != null) {
                if (isHotel) {
                    reservation.setCheckOut(date.atTime(12, 0));
                }
                reservation.setEndDate(date.atTime(isHotel ? 12 : 18, 0));
            }
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e1) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(dateStr, formatter);
            } catch (Exception e2) {
                log.warn("Unable to parse date: {}", dateStr);
                return null;
            }
        }
    }
}
