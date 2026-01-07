package com.ahmedyassin.TravelSmart.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "etape")
public class Etape {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Integer ordre; // Order/sequence of the step

    @Column(nullable = false)
    private String lieu; // Location/place

    @Column(length = 1000)
    private String description;

    @Column
    private Integer dureeJours; // Duration in days at this step

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circuit_id")
    @JsonBackReference("circuit-etapes")
    private Circuit circuit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel; // Optional hotel at this step
}
