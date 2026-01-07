package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO pour la réponse de l'API AviationStack Airlines
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationStackAirlineResponse {

    @JsonProperty("data")
    private List<AirlineData> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AirlineData {
        @JsonProperty("airline_name")
        private String airlineName;

        @JsonProperty("iata_code")
        private String iataCode;

        @JsonProperty("icao_code")
        private String icaoCode;

        @JsonProperty("country_name")
        private String countryName;

        @JsonProperty("country_iso2")
        private String countryIso2;

        @JsonProperty("callsign")
        private String callsign;

        @JsonProperty("status")
        private String status;
    }
}

