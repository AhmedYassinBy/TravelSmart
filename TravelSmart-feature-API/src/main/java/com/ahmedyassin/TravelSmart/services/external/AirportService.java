package com.ahmedyassin.TravelSmart.services.external;

import com.ahmedyassin.TravelSmart.clients.AviationStackApiClient;
import com.ahmedyassin.TravelSmart.dto.external.AirportDTO;
import com.ahmedyassin.TravelSmart.dto.external.response.AviationStackAirportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirportService {

    private final AviationStackApiClient aviationStackApiClient;

    /**
     * Rechercher des aéroports
     */
    public List<AirportDTO> searchAirports(String airportName, String country) {
        log.info("Searching airports with name: {} in country: {}", airportName, country);

        try {
            AviationStackAirportResponse response = aviationStackApiClient.searchAirports(airportName, country);

            if (response == null || response.getData() == null) {
                log.warn("No airports found");
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .map(this::mapAirportToDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching airports: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Mapper Airport Data vers DTO
     */
    private AirportDTO mapAirportToDTO(AviationStackAirportResponse.AirportData airportData) {
        return AirportDTO.builder()
                .airportCode(airportData.getIataCode())
                .airportName(airportData.getAirportName())
                .city(airportData.getCityIataCode())
                .country(airportData.getCountryName())
                .countryCode(airportData.getCountryIso2())
                .latitude(airportData.getLatitude())
                .longitude(airportData.getLongitude())
                .timezone(airportData.getTimezone())
                .iataCode(airportData.getIataCode())
                .icaoCode(airportData.getIcaoCode())
                .build();
    }
}

