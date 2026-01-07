package com.ahmedyassin.TravelSmart.controllers.external;

import com.ahmedyassin.TravelSmart.dto.external.HotelDTO;
import com.ahmedyassin.TravelSmart.services.external.ExternalHotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HotelController {

    private final ExternalHotelService hotelService;

    /**
     * Rechercher des hôtels par code ville (Amadeus)
     * Example: GET /api/hotels/search?cityCode=PAR
     */
    @GetMapping("/search")
    public ResponseEntity<List<HotelDTO>> searchHotelsByCity(@RequestParam String cityCode) {
        log.info("REST Request to search hotels by city code: {}", cityCode);
        List<HotelDTO> hotels = hotelService.searchHotelsByCity(cityCode);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Rechercher des hôtels via Booking API
     * Example: GET
     * /api/hotels/booking?destination=-553173&checkinDate=2024-01-15&checkoutDate=2024-01-20
     */
    @GetMapping("/booking")
    public ResponseEntity<List<HotelDTO>> searchHotelsViaBooking(
            @RequestParam String destination,
            @RequestParam String checkinDate,
            @RequestParam String checkoutDate) {
        log.info("REST Request to search hotels via Booking API for destination: {}", destination);
        List<HotelDTO> hotels = hotelService.searchHotelsViaBooking(destination, checkinDate, checkoutDate);
        return ResponseEntity.ok(hotels);
    }
}
