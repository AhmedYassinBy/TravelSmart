package com.ahmedyassin.TravelSmart.repositories;

import com.ahmedyassin.TravelSmart.entities.Circuit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CircuitRepository extends JpaRepository<Circuit, UUID> {
}

