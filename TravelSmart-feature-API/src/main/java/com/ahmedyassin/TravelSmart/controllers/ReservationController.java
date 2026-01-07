package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.Reservation;
import com.ahmedyassin.TravelSmart.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Get all reservations for a client
     */
    @GetMapping
    public ResponseEntity<?> getReservations(@RequestParam String email) {
        log.info("GET /api/reservation?email={}", email);

        try {
            List<Reservation> reservations = reservationService.getReservationsByEmail(email);
            log.info("✅ {} reservation(s) found for {}", reservations.size(), email);

            // Convert to Map to ensure all fields are included
            List<Map<String, Object>> response = new java.util.ArrayList<>();
            for (Reservation r : reservations) {
                Map<String, Object> resMap = new HashMap<>();
                resMap.put("id", r.getId().toString());
                resMap.put("clientEmail", r.getClientEmail());
                resMap.put("offerType", r.getOfferType());
                resMap.put("offerId", r.getOfferId().toString());
                resMap.put("offerName", r.getOfferName());
                resMap.put("price", r.getPrice());
                resMap.put("bookingDate", r.getBookingDate().toString());
                resMap.put("status", r.getStatus().toString());
                resMap.put("paymentMethod", r.getPaymentMethod());

                if (r.getStartDate() != null) resMap.put("startDate", r.getStartDate().toString());
                if (r.getEndDate() != null) resMap.put("endDate", r.getEndDate().toString());
                if (r.getCheckIn() != null) resMap.put("checkIn", r.getCheckIn().toString());
                if (r.getCheckOut() != null) resMap.put("checkOut", r.getCheckOut().toString());
                if (r.getAdultsCount() != null) resMap.put("adultsCount", r.getAdultsCount());
                if (r.getChildrenCount() != null) resMap.put("childrenCount", r.getChildrenCount());
                if (r.getFormula() != null) resMap.put("formula", r.getFormula());
                if (r.getChildrenAges() != null) resMap.put("childrenAges", r.getChildrenAges());
                if (r.getHotelLevel() != null) resMap.put("hotelLevel", r.getHotelLevel());
                if (r.getFlightClass() != null) resMap.put("flightClass", r.getFlightClass());
                if (r.getSelectedActivities() != null) resMap.put("selectedActivities", r.getSelectedActivities());
                if (r.getPriceBreakdown() != null) resMap.put("priceBreakdown", r.getPriceBreakdown());

                response.add(resMap);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error fetching reservations: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get reservation details
     */
    @GetMapping("/{reservationId}/details")
    public ResponseEntity<?> getReservationDetails(@PathVariable String reservationId) {
        log.info("GET /api/reservation/{}/details", reservationId);

        try {
            UUID id = UUID.fromString(reservationId);
            Map<String, Object> details = reservationService.getReservationDetails(id);
            log.info("✅ Reservation {} details retrieved", reservationId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            log.error("❌ Error fetching reservation details: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a new reservation
     */
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Map<String, String> body) {
        log.info("POST /api/reservation");
        log.info("Body received: {}", body);

        try {
            // Extract parameters from body
            String email = body.get("email");
            String paymentMethod = body.get("paymentMethod");

            // Determine offer type and ID
            String offerType = null;
            UUID offerId = null;
            Double price = null;

            if (body.containsKey("hotelId")) {
                offerType = "hotel";
                offerId = UUID.fromString(body.get("hotelId"));
                price = Double.parseDouble(body.get("totalPrice"));
                log.info("HOTEL reservation: hotelId={}, roomType={}", body.get("hotelId"), body.get("roomType"));
            } else if (body.containsKey("flightId")) {
                offerType = "flight";
                offerId = UUID.fromString(body.get("flightId"));
                price = body.containsKey("totalPrice") ? Double.parseDouble(body.get("totalPrice")) :
                        body.containsKey("price") ? Double.parseDouble(body.get("price")) : 0.0;
                log.info("FLIGHT reservation: flightId={}, price={}", body.get("flightId"), price);
            } else if (body.containsKey("circuitId")) {
                offerType = "circuit";
                offerId = UUID.fromString(body.get("circuitId"));
                price = body.containsKey("totalPrice") ? Double.parseDouble(body.get("totalPrice")) :
                        body.containsKey("price") ? Double.parseDouble(body.get("price")) : 0.0;
                log.info("CIRCUIT reservation: circuitId={}, price={}", body.get("circuitId"), price);
            } else {
                log.error("❌ Unknown offer type in body");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Missing offer type (hotelId, flightId or circuitId required)"));
            }

            // Extract optional parameters
            String roomType = body.get("roomType");
            String checkInDate = body.get("checkInDate");
            String checkOutDate = body.get("checkOutDate");
            Integer adultsCount = body.containsKey("adultsCount") ? Integer.parseInt(body.get("adultsCount")) : null;
            Integer childrenCount = body.containsKey("childrenCount") ? Integer.parseInt(body.get("childrenCount")) : null;
            String formula = body.get("formula");

            // Circuit-specific
            String childrenAges = body.get("childrenAges");
            String hotelLevel = body.get("hotelLevel");
            String flightClass = body.get("flightClass");
            String selectedActivities = body.get("selectedActivities");
            String priceBreakdown = body.get("priceBreakdown");

            log.info("Params: email={}, offerId={}, offerType={}, price={}, paymentMethod={}",
                    email, offerId, offerType, price, paymentMethod);

            Map<String, Object> response = reservationService.createReservation(
                    email, offerId, offerType, price, paymentMethod, roomType,
                    checkInDate, checkOutDate, adultsCount, childrenCount, formula,
                    childrenAges, hotelLevel, flightClass, selectedActivities, priceBreakdown);

            log.info("✅ Reservation created successfully");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid data: " + e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel a reservation
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable String reservationId) {
        log.info("DELETE /api/reservation/{}", reservationId);

        try {
            UUID id = UUID.fromString(reservationId);
            Map<String, Object> result = reservationService.cancelReservation(id);

            if ((Boolean) result.get("success")) {
                log.info("✅ Reservation {} cancelled", reservationId);
                return ResponseEntity.ok(result);
            } else {
                log.warn("⚠️ Cancellation refused: {}", result.get("error"));
                return ResponseEntity.badRequest().body(result);
            }
        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid ID: {}", reservationId);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", "Invalid reservation ID"));
        } catch (Exception e) {
            log.error("❌ Error cancelling: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
