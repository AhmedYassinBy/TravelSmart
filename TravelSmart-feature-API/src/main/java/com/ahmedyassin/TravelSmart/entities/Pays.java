package com.ahmedyassin.TravelSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "pays")
public class Pays {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    // Inverse ManyToMany relation with Circuit
    @ManyToMany(mappedBy = "pays")
    @JsonIgnore
    private List<Circuit> circuits = new ArrayList<>();
}
