package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Flight;
import com.ahmedyassin.TravelSmart.repositories.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final FlightRepository flightRepository;

    @Transactional
    public Flight create(Flight f){
        return flightRepository.save(f);
    }

    @Transactional
    public Flight update(UUID id, Flight updates){
        Flight existing = flightRepository.findById(id).orElseThrow(() -> new RuntimeException("Flight not found"));
        existing.setAirline(updates.getAirline());
        existing.setFlightNumber(updates.getFlightNumber());
        existing.setOrigin(updates.getOrigin());
        existing.setDestination(updates.getDestination());
        existing.setDepartureTime(updates.getDepartureTime());
        existing.setArrivalTime(updates.getArrivalTime());
        existing.setPrice(updates.getPrice());
        existing.setSeatsAvailable(updates.getSeatsAvailable());
        return flightRepository.save(existing);
    }

    public Optional<Flight> findById(UUID id){
        return flightRepository.findById(id);
    }

    public List<Flight> findAll(){
        return flightRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        flightRepository.deleteById(id);
    }
}

