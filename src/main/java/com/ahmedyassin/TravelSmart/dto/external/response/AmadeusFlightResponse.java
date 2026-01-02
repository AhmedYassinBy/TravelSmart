package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API Amadeus Flight Offers
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmadeusFlightResponse {

    @JsonProperty("data")
    private List<FlightOffer> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightOffer {
        @JsonProperty("id")
        private String id;

        @JsonProperty("price")
        private Price price;

        @JsonProperty("itineraries")
        private List<Itinerary> itineraries;

        @JsonProperty("numberOfBookableSeats")
        private Integer numberOfBookableSeats;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Price {
        @JsonProperty("total")
        private String total;

        @JsonProperty("currency")
        private String currency;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Itinerary {
        @JsonProperty("duration")
        private String duration;

        @JsonProperty("segments")
        private List<Segment> segments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Segment {
        @JsonProperty("departure")
        private Endpoint departure;

        @JsonProperty("arrival")
        private Endpoint arrival;

        @JsonProperty("carrierCode")
        private String carrierCode;

        @JsonProperty("number")
        private String number;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Endpoint {
        @JsonProperty("iataCode")
        private String iataCode;

        @JsonProperty("at")
        private String at;
    }
}

