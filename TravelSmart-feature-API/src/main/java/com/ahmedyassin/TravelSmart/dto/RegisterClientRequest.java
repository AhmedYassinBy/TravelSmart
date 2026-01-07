package com.ahmedyassin.TravelSmart.dto;

import lombok.Data;

@Data
public class RegisterClientRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String telephone;
}
