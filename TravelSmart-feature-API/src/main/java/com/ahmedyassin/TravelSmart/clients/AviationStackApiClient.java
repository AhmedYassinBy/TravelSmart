package com.ahmedyassin.TravelSmart.clients;

import com.ahmedyassin.TravelSmart.dto.external.response.AviationStackAirlineResponse;
import com.ahmedyassin.TravelSmart.dto.external.response.AviationStackAirportResponse;
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
public class AviationStackApiClient {

    private final WebClient webClient;

    @Value("${aviationstack.api.key}")
    private String apiKey;

    public AviationStackApiClient(@Qualifier("aviationStackWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Rechercher des compagnies aériennes
     */
    public AviationStackAirlineResponse searchAirlines(String airlineName) {
        log.info("Searching airlines with name: {}", airlineName);

        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/airlines")
                                .queryParam("access_key", apiKey);

                        if (airlineName != null && !airlineName.isEmpty()) {
                            builder.queryParam("airline_name", airlineName);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("AviationStack 4xx error: {}", errorBody);
                                        return Mono.error(new ExternalApiException("Invalid request to AviationStack API: " + errorBody));
                                    }))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("AviationStack API server error")))
                    .bodyToMono(AviationStackAirlineResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error searching airlines: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to search airlines from AviationStack", e);
        }
    }

    /**
     * Rechercher des aéroports
     */
    public AviationStackAirportResponse searchAirports(String airportName, String country) {
        log.info("Searching airports with name: {} in country: {}", airportName, country);

        try {
            return webClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/airports")
                                .queryParam("access_key", apiKey);

                        if (airportName != null && !airportName.isEmpty()) {
                            builder.queryParam("search", airportName);
                        }

                        if (country != null && !country.isEmpty()) {
                            builder.queryParam("country_name", country);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("AviationStack 4xx error: {}", errorBody);
                                        return Mono.error(new ExternalApiException("Invalid request to AviationStack API: " + errorBody));
                                    }))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("AviationStack API server error")))
                    .bodyToMono(AviationStackAirportResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error searching airports: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to search airports from AviationStack", e);
        }
    }
}

