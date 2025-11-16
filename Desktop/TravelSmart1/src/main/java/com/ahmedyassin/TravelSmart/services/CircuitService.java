package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Activity;
import com.ahmedyassin.TravelSmart.entities.Circuit;
import com.ahmedyassin.TravelSmart.entities.Hotel;
import com.ahmedyassin.TravelSmart.repositories.ActivityRepository;
import com.ahmedyassin.TravelSmart.repositories.CircuitRepository;
import com.ahmedyassin.TravelSmart.repositories.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CircuitService {
    private final CircuitRepository circuitRepository;
    private final HotelRepository hotelRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public Circuit create(Circuit c){
        return circuitRepository.save(c);
    }

    @Transactional
    public Circuit update(UUID id, Circuit updates){
        Circuit existing = circuitRepository.findById(id).orElseThrow(() -> new RuntimeException("Circuit not found"));
        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        return circuitRepository.save(existing);
    }

    public Optional<Circuit> findById(UUID id){
        return circuitRepository.findById(id);
    }

    public List<Circuit> findAll(){
        return circuitRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        circuitRepository.deleteById(id);
    }

    @Transactional
    public Circuit addHotel(UUID circuitId, UUID hotelId){
        Circuit circuit = circuitRepository.findById(circuitId).orElseThrow(() -> new RuntimeException("Circuit not found"));
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("Hotel not found"));
        circuit.getHotels().add(hotel);
        return circuitRepository.save(circuit);
    }

    @Transactional
    public Circuit addActivity(UUID circuitId, UUID activityId){
        Circuit circuit = circuitRepository.findById(circuitId).orElseThrow(() -> new RuntimeException("Circuit not found"));
        Activity activity = activityRepository.findById(activityId).orElseThrow(() -> new RuntimeException("Activity not found"));
        circuit.getActivities().add(activity);
        return circuitRepository.save(circuit);
    }
}

