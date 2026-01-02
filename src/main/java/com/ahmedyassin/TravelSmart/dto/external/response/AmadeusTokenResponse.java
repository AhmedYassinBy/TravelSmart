package com.ahmedyassin.TravelSmart.dto.external.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO pour la réponse d'authentification Amadeus
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmadeusTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Long expiresIn;
}

