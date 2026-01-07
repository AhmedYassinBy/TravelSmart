package com.ahmedyassin.TravelSmart.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
}
