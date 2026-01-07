package com.ahmedyassin.TravelSmart.clients;

import com.ahmedyassin.TravelSmart.dto.external.response.BookingHotelResponse;
import com.ahmedyassin.TravelSmart.exceptions.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class BookingApiClient {

    private final WebClient webClient;

    @Value("${rapidapi.booking.key}")
    private String apiKey;

    @Value("${rapidapi.booking.host}")
    private String apiHost;

    public BookingApiClient(@Qualifier("rapidApiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Rechercher des hôtels par destination
     */
    public BookingHotelResponse searchHotels(String destination, String checkinDate, String checkoutDate) {
        log.info("Searching hotels in destination: {} from {} to {}", destination, checkinDate, checkoutDate);
        log.debug("Using RapidAPI Key: {}...", apiKey != null && !apiKey.isEmpty() ? apiKey.substring(0, Math.min(10, apiKey.length())) : "EMPTY");
        log.debug("Using RapidAPI Host: {}", apiHost);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/hotels/search")
                            .queryParam("dest_type", "city")
                            .queryParam("dest_id", destination)
                            .queryParam("checkin_date", checkinDate)
                            .queryParam("checkout_date", checkoutDate)
                            .queryParam("adults_number", 1)
                            .queryParam("order_by", "popularity")
                            .queryParam("units", "metric")
                            .queryParam("room_number", 1)
                            .build())
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", apiHost)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Booking API 4xx error: {} | Status: {}", errorBody, clientResponse.statusCode());
                                        return Mono.error(new ExternalApiException("Invalid request to Booking API (Status " + clientResponse.statusCode() + "): " + errorBody));
                                    }))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("Booking API server error (Status " + clientResponse.statusCode() + ")")))
                    .bodyToMono(BookingHotelResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error searching hotels from Booking. Type: {}, Message: {}, Cause: {}", 
                     e.getClass().getSimpleName(), 
                     e.getMessage(), 
                     e.getCause() != null ? e.getCause().getMessage() : "N/A");
            throw new ExternalApiException("Failed to search hotels from Booking API: " + e.getMessage(), e);
        }
    }
}

