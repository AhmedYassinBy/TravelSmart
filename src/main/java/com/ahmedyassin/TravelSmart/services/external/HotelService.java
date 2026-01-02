package com.ahmedyassin.TravelSmart.services.external;

import com.ahmedyassin.TravelSmart.clients.AmadeusApiClient;
import com.ahmedyassin.TravelSmart.clients.BookingApiClient;
import com.ahmedyassin.TravelSmart.dto.external.HotelDTO;
import com.ahmedyassin.TravelSmart.dto.external.response.AmadeusHotelResponse;
import com.ahmedyassin.TravelSmart.dto.external.response.BookingHotelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {

    private final AmadeusApiClient amadeusApiClient;
    private final BookingApiClient bookingApiClient;

    /**
     * Rechercher des hôtels via Amadeus
     */
    public List<HotelDTO> searchHotelsByCity(String cityCode) {
        log.info("Searching hotels by city code: {}", cityCode);

        try {
            AmadeusHotelResponse response = amadeusApiClient.searchHotelsByCity(cityCode);

            if (response == null || response.getData() == null) {
                log.warn("No hotels found for city: {}", cityCode);
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .map(this::mapAmadeusHotelToDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Rechercher des hôtels via Booking API
     */
    public List<HotelDTO> searchHotelsViaBooking(String destination, String checkinDate, String checkoutDate) {
        log.info("Searching hotels via Booking API for destination: {}", destination);

        try {
            BookingHotelResponse response = bookingApiClient.searchHotels(destination, checkinDate, checkoutDate);

            if (response == null || response.getResult() == null) {
                log.warn("No hotels found for destination: {}", destination);
                return Collections.emptyList();
            }

            return response.getResult().stream()
                    .map(this::mapBookingHotelToDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching hotels via Booking: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Mapper Amadeus Hotel vers DTO
     */
    private HotelDTO mapAmadeusHotelToDTO(AmadeusHotelResponse.HotelData hotelData) {
        return HotelDTO.builder()
                .hotelId(hotelData.getHotelId())
                .name(hotelData.getName())
                .address(hotelData.getAddress() != null && hotelData.getAddress().getLines() != null
                        ? String.join(", ", hotelData.getAddress().getLines())
                        : null)
                .city(hotelData.getAddress() != null ? hotelData.getAddress().getCityName() : null)
                .country(hotelData.getAddress() != null ? hotelData.getAddress().getCountryCode() : null)
                .latitude(hotelData.getLatitude())
                .longitude(hotelData.getLongitude())
                .stars(parseStars(hotelData.getRating()))
                .amenities(hotelData.getAmenities())
                .build();
    }

    /**
     * Mapper Booking Hotel vers DTO
     */
    private HotelDTO mapBookingHotelToDTO(BookingHotelResponse.HotelResult hotelResult) {
        return HotelDTO.builder()
                .hotelId(hotelResult.getHotelId())
                .name(hotelResult.getHotelName())
                .address(hotelResult.getAddress())
                .city(hotelResult.getCity())
                .country(hotelResult.getCountry())
                .latitude(hotelResult.getLatitude())
                .longitude(hotelResult.getLongitude())
                .stars(hotelResult.getHotelClass())
                .minPrice(hotelResult.getMinTotalPrice() != null
                        ? BigDecimal.valueOf(hotelResult.getMinTotalPrice())
                        : null)
                .currency(hotelResult.getCurrencyCode())
                .rating(hotelResult.getReviewScore())
                .reviewCount(hotelResult.getReviewNr())
                .imageUrl(hotelResult.getMainPhotoUrl())
                .build();
    }

    /**
     * Parser le rating en nombre d'étoiles
     */
    private Integer parseStars(String rating) {
        if (rating == null || rating.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(rating.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

