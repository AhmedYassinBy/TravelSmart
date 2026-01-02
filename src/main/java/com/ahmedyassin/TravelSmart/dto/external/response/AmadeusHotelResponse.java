package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API Amadeus Hotel List
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmadeusHotelResponse {

    @JsonProperty("data")
    private List<HotelData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HotelData {
        @JsonProperty("hotelId")
        private String hotelId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("rating")
        private String rating;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("address")
        private Address address;

        @JsonProperty("amenities")
        private List<String> amenities;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("cityName")
        private String cityName;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("lines")
        private List<String> lines;
    }
}

