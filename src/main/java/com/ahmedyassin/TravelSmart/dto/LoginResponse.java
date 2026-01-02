package com.ahmedyassin.TravelSmart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String jwt;
    private String username;
    private String errorMessage;

    public LoginResponse(String jwt, String username) {
        this.jwt = jwt;
        this.username = username;
    }
}