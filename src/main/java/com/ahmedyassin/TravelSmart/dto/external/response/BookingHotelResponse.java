package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API Booking.com via RapidAPI
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingHotelResponse {

    @JsonProperty("result")
    private List<HotelResult> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HotelResult {
        @JsonProperty("hotel_id")
        private String hotelId;

        @JsonProperty("hotel_name")
        private String hotelName;

        @JsonProperty("address")
        private String address;

        @JsonProperty("city")
        private String city;

        @JsonProperty("country_trans")
        private String country;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("class")
        private Integer hotelClass;

        @JsonProperty("min_total_price")
        private Double minTotalPrice;

        @JsonProperty("currencycode")
        private String currencyCode;

        @JsonProperty("review_score")
        private Double reviewScore;

        @JsonProperty("review_nr")
        private Integer reviewNr;

        @JsonProperty("main_photo_url")
        private String mainPhotoUrl;
    }
}

