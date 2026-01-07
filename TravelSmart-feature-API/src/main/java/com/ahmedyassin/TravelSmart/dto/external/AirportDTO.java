package com.ahmedyassin.TravelSmart.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirportDTO {
    private String airportCode;
    private String airportName;
    private String city;
    private String country;
    private String countryCode;
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String iataCode;
    private String icaoCode;
}

