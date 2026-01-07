package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.Hotel;
import com.ahmedyassin.TravelSmart.services.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Mobile-facing Hotel Controller
 * Provides read-only access to local database hotels for the mobile app
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/hotels")
@RequiredArgsConstructor
public class MobileHotelController {

    private final HotelService hotelService;

    /**
     * Get all active hotels
     */
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels() {
        log.info("GET /api/mobile/hotels");
        List<Hotel> hotels = hotelService.getAllActiveHotels();
        log.info("✅ {} hotel(s) found", hotels.size());
        return ResponseEntity.ok(hotels);
    }

    /**
     * Get hotel by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable UUID id) {
        log.info("GET /api/mobile/hotels/{}", id);
        Hotel hotel = hotelService.getHotelById(id);
        return ResponseEntity.ok(hotel);
    }

    /**
     * Get hotels by city
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<List<Hotel>> getHotelsByCity(@PathVariable String city) {
        log.info("GET /api/mobile/hotels/city/{}", city);
        List<Hotel> hotels = hotelService.getHotelsByCity(city);
        log.info("✅ {} hotel(s) found in {}", hotels.size(), city);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Get hotels by star rating
     */
    @GetMapping("/stars/{stars}")
    public ResponseEntity<List<Hotel>> getHotelsByStars(@PathVariable Integer stars) {
        log.info("GET /api/mobile/hotels/stars/{}", stars);
        List<Hotel> hotels = hotelService.getHotelsByStars(stars);
        log.info("✅ {} {}-star hotel(s) found", hotels.size(), stars);
        return ResponseEntity.ok(hotels);
    }

    /**
     * Search hotels with filters
     */
    @GetMapping("/search")
    public ResponseEntity<List<Hotel>> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        log.info("GET /api/mobile/hotels/search?city={}&minPrice={}&maxPrice={}",
                city, minPrice, maxPrice);
        List<Hotel> hotels = hotelService.searchHotels(city, minPrice, maxPrice, sortBy, sortOrder);
        log.info("✅ {} hotel(s) found", hotels.size());
        return ResponseEntity.ok(hotels);
    }
}
