package com.ahmedyassin.TravelSmart.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "traveler")
public class Traveler {

@Id
@GeneratedValue(generator = "uuid2")
@GenericGenerator(name="uuid2",strategy = "uuid2")
@Column(name = "id",unique = true, nullable = false)
    private UUID id;


@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "travel_Request_id")
private TravelRequest travelRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

@Column(name = "first_name")
private String firstName;


@Column(name = "last_name")
private String lastName;

@Column(name = "position")
private String position;

@Column(name = "passeport_file_name")
private String PasseportFileName;

@Column(name = "passeport_uploaded_at")
    private LocalDateTime PasseportUploadedAt;
}
