package com.ahmedyassin.TravelSmart.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDTO {
    private String flightNumber;
    private String airlineName;
    private String airlineCode;
    private String departureAirport;
    private String departureAirportCode;
    private String arrivalAirport;
    private String arrivalAirportCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String duration;
    private BigDecimal price;
    private String currency;
    private String flightStatus;
    private Integer availableSeats;
    private String cabinClass;
}

