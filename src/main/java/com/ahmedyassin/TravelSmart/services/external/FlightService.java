package com.ahmedyassin.TravelSmart.services.external;

import com.ahmedyassin.TravelSmart.clients.AmadeusApiClient;
import com.ahmedyassin.TravelSmart.dto.external.FlightDTO;
import com.ahmedyassin.TravelSmart.dto.external.response.AmadeusFlightResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final AmadeusApiClient amadeusApiClient;

    /**
     * Rechercher des vols
     */
    public List<FlightDTO> searchFlights(String origin, String destination, String departureDate, Integer adults) {
        log.info("Searching flights from {} to {} on {}", origin, destination, departureDate);

        try {
            AmadeusFlightResponse response = amadeusApiClient.searchFlightOffers(origin, destination, departureDate, adults);

            if (response == null || response.getData() == null) {
                log.warn("No flights found for route: {} -> {}", origin, destination);
                return Collections.emptyList();
            }

            return response.getData().stream()
                    .flatMap(offer -> offer.getItineraries().stream()
                            .flatMap(itinerary -> itinerary.getSegments().stream()
                                    .map(segment -> mapFlightToDTO(segment, offer, itinerary))))
                    .toList();
        } catch (Exception e) {
            log.error("Error searching flights: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Mapper un segment de vol vers DTO
     */
    private FlightDTO mapFlightToDTO(AmadeusFlightResponse.Segment segment,
                                     AmadeusFlightResponse.FlightOffer offer,
                                     AmadeusFlightResponse.Itinerary itinerary) {
        return FlightDTO.builder()
                .flightNumber(segment.getCarrierCode() + segment.getNumber())
                .airlineCode(segment.getCarrierCode())
                .departureAirportCode(segment.getDeparture().getIataCode())
                .arrivalAirportCode(segment.getArrival().getIataCode())
                .departureTime(parseDateTime(segment.getDeparture().getAt()))
                .arrivalTime(parseDateTime(segment.getArrival().getAt()))
                .duration(itinerary.getDuration())
                .price(offer.getPrice() != null && offer.getPrice().getTotal() != null
                        ? new BigDecimal(offer.getPrice().getTotal())
                        : null)
                .currency(offer.getPrice() != null ? offer.getPrice().getCurrency() : null)
                .availableSeats(offer.getNumberOfBookableSeats())
                .build();
    }

    /**
     * Parser une date-time ISO string
     */
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", dateTimeString);
            return null;
        }
    }
}

