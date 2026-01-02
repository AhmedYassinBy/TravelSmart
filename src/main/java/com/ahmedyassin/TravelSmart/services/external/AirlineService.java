package com.ahmedyassin.TravelSmart.services.external;

import com.ahmedyassin.TravelSmart.clients.AviationStackApiClient;
import com.ahmedyassin.TravelSmart.dto.external.AirlineDTO;
import com.ahmedyassin.TravelSmart.dto.external.response.AviationStackAirlineResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirlineService {

    private final AviationStackApiClient aviationStackApiClient;

    /**
     * Rechercher des compagnies aériennes
     */
    public List<AirlineDTO> searchAirlines(String airlineName) {
        log.info("Searching airlines with name: {}", airlineName);

        try {
            AviationStackAirlineResponse response = aviationStackApiClient.searchAirlines(airlineName);

            if (response == null || response.getData() == null) {
                log.warn("No airlines found");
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .map(this::mapAirlineToDTO)
                    .toList();
        } catch (Exception e) {
            log.error("Error searching airlines: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Mapper Airline Data vers DTO
     */
    private AirlineDTO mapAirlineToDTO(AviationStackAirlineResponse.AirlineData airlineData) {
        return AirlineDTO.builder()
                .airlineCode(airlineData.getIataCode())
                .airlineName(airlineData.getAirlineName())
                .iataCode(airlineData.getIataCode())
                .icaoCode(airlineData.getIcaoCode())
                .country(airlineData.getCountryName())
                .callsign(airlineData.getCallsign())
                .active("active".equalsIgnoreCase(airlineData.getStatus()))
                .build();
    }
}

