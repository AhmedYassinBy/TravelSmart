package com.ahmedyassin.TravelSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "circuits")
public class Circuit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column
    private Integer duree; // Total duration in days

    @Column
    private Double prix; // Price

    @Column
    private String imageUrl; // Cover image

    @Column
    private String difficulty; // easy, moderate, challenging

    @Column
    private Integer maxParticipants;

    // Mobile-specific fields
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "circuit_destinations", joinColumns = @JoinColumn(name = "circuit_id"))
    @Column(name = "destination")
    private List<String> destinations = new ArrayList<>();

    @Column(name = "type")
    private String type; // CULTUREL, AVENTURE, DETENTE, MIXTE

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "circuit_included", joinColumns = @JoinColumn(name = "circuit_id"))
    @Column(name = "item")
    private List<String> included = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "circuit_highlights", joinColumns = @JoinColumn(name = "circuit_id"))
    @Column(name = "highlight")
    private List<String> highlights = new ArrayList<>();

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Admin-specific: Etapes (steps with order and location)
    @OneToMany(mappedBy = "circuit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("ordre ASC")
    @JsonManagedReference("circuit-etapes")
    private List<Etape> etapes = new ArrayList<>();

    // Mobile-specific: Program (daily schedule)
    @OneToMany(mappedBy = "circuit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dayNumber ASC")
    @JsonManagedReference("circuit-days")
    private List<CircuitDay> program = new ArrayList<>();

    // Mobile-specific: Circuit Activities (optional activities with pricing)
    @OneToMany(mappedBy = "circuit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference("circuit-activities")
    private List<CircuitActivity> circuitActivities = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "circuit_hotel",
            joinColumns = @JoinColumn(name = "circuit_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id"))
    private List<Hotel> hotels = new ArrayList<>();

    // Admin-specific: Activity entities
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "circuit_activity",
            joinColumns = @JoinColumn(name = "circuit_id"),
            inverseJoinColumns = @JoinColumn(name = "activity_id"))
    private List<Activity> activities = new ArrayList<>();

    // Country association for mobile
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "circuit_pays",
            joinColumns = @JoinColumn(name = "circuit_id"),
            inverseJoinColumns = @JoinColumn(name = "pays_id")
    )
    private List<Pays> pays = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (isActive == null) {
            isActive = true;
        }
    }

    // Helper method to add etape
    public void addEtape(Etape etape) {
        etapes.add(etape);
        etape.setCircuit(this);
    }

    // Helper method to remove etape
    public void removeEtape(Etape etape) {
        etapes.remove(etape);
        etape.setCircuit(null);
    }
}

