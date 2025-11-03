package com.ahmedyassin.TravelSmart.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @OneToOne(fetch = FetchType.LAZY) // A team has one team lead

    @JoinColumn(name = "team_lead_id", unique = true)

    private User teamLead;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<User> members;
}