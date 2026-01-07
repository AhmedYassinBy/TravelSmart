package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API AviationStack Airports
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationStackAirportResponse {

    @JsonProperty("data")
    private List<AirportData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirportData {
        @JsonProperty("airport_name")
        private String airportName;

        @JsonProperty("iata_code")
        private String iataCode;

        @JsonProperty("icao_code")
        private String icaoCode;

        @JsonProperty("city_iata_code")
        private String cityIataCode;

        @JsonProperty("country_name")
        private String countryName;

        @JsonProperty("country_iso2")
        private String countryIso2;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        @JsonProperty("timezone")
        private String timezone;

        @JsonProperty("gmt")
        private String gmt;
    }
}

