package com.ahmedyassin.TravelSmart.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDTO {
    private String hotelId;
    private String name;
    private String address;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer stars;
    private BigDecimal minPrice;
    private String currency;
    private List<String> amenities;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private String description;
}

