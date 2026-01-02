package com.ahmedyassin.TravelSmart.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirlineDTO {
    private String airlineCode;
    private String airlineName;
    private String iataCode;
    private String icaoCode;
    private String country;
    private String callsign;
    private Boolean active;
}

