package com.ahmedyassin.TravelSmart.clients;

import com.ahmedyassin.TravelSmart.dto.external.response.AmadeusFlightResponse;
import com.ahmedyassin.TravelSmart.dto.external.response.AmadeusHotelResponse;
import com.ahmedyassin.TravelSmart.dto.external.response.AmadeusTokenResponse;
import com.ahmedyassin.TravelSmart.exceptions.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class AmadeusApiClient {

    private final WebClient webClient;

    @Value("${amadeus.api.key}")
    private String apiKey;

    @Value("${amadeus.api.secret}")
    private String apiSecret;

    private String accessToken;
    private LocalDateTime tokenExpiryTime;

    public AmadeusApiClient(@Qualifier("amadeusWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Obtenir un token d'accès Amadeus
     */
    private String getAccessToken() {
        // Vérifier si le token existe et est valide
        if (accessToken != null && tokenExpiryTime != null && LocalDateTime.now().isBefore(tokenExpiryTime)) {
            return accessToken;
        }

        log.info("Requesting new Amadeus access token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", apiKey);
        formData.add("client_secret", apiSecret);

        try {
            AmadeusTokenResponse response = webClient.post()
                    .uri("/v1/security/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            Mono.error(new ExternalApiException("Amadeus Authentication Failed: Invalid credentials")))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("Amadeus API is unavailable")))
                    .bodyToMono(AmadeusTokenResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.getAccessToken() != null) {
                this.accessToken = response.getAccessToken();
                // Le token expire dans expiresIn secondes, on le rafraîchit 5 minutes avant
                this.tokenExpiryTime = LocalDateTime.now().plusSeconds(response.getExpiresIn() - 300);
                log.info("Amadeus access token obtained successfully");
                return this.accessToken;
            }

            throw new ExternalApiException("Failed to obtain Amadeus access token");
        } catch (Exception e) {
            log.error("Error getting Amadeus access token: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to authenticate with Amadeus API", e);
        }
    }

    /**
     * Rechercher des hôtels par ville
     */
    public AmadeusHotelResponse searchHotelsByCity(String cityCode) {
        log.info("Searching hotels in city: {}", cityCode);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/reference-data/locations/hotels/by-city")
                            .queryParam("cityCode", cityCode)
                            .build())
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Amadeus 4xx error: {}", errorBody);
                                        return Mono.error(new ExternalApiException("Invalid request to Amadeus API: " + errorBody));
                                    }))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("Amadeus API server error")))
                    .bodyToMono(AmadeusHotelResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error searching hotels: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to search hotels from Amadeus", e);
        }
    }

    /**
     * Rechercher des offres de vols
     */
    public AmadeusFlightResponse searchFlightOffers(String origin, String destination, String departureDate, Integer adults) {
        log.info("Searching flights from {} to {} on {}", origin, destination, departureDate);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/shopping/flight-offers")
                            .queryParam("originLocationCode", origin)
                            .queryParam("destinationLocationCode", destination)
                            .queryParam("departureDate", departureDate)
                            .queryParam("adults", adults != null ? adults : 1)
                            .queryParam("max", 10)
                            .build())
                    .header("Authorization", "Bearer " + getAccessToken())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Amadeus 4xx error: {}", errorBody);
                                        return Mono.error(new ExternalApiException("Invalid flight search request: " + errorBody));
                                    }))
                    .onStatus(status -> status.is5xxServerError(), clientResponse ->
                            Mono.error(new ExternalApiException("Amadeus API server error")))
                    .bodyToMono(AmadeusFlightResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("Error searching flights: {}", e.getMessage(), e);
            throw new ExternalApiException("Failed to search flights from Amadeus", e);
        }
    }
}

