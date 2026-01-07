package com.ahmedyassin.TravelSmart.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String roomNumber;

    @Column(name = "room_type")
    private String roomType;

    @Column
    private String type;

    @Column
    private Double price;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(length = 1000)
    private String description;

    @Column(name = "is_available")
    private Boolean isAvailable = true;

    @Column(name = "available")
    private Boolean available = true;

    @Column(name = "view_type")
    private String viewType;

    @Column(name = "bed_type")
    private String bedType;

    @Column(name = "size_sqm")
    private Double sizeSqm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    @JsonBackReference
    private Hotel hotel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onPrePersist() {
        if (isAvailable == null) {
            isAvailable = true;
        }
        if (available == null) {
            available = true;
        }
    }
}
