package com.ahmedyassin.TravelSmart.repositories;

import com.ahmedyassin.TravelSmart.entities.Etape;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EtapeRepository extends JpaRepository<Etape, UUID> {
    List<Etape> findByCircuitIdOrderByOrdreAsc(UUID circuitId);
    void deleteByCircuitId(UUID circuitId);
}
