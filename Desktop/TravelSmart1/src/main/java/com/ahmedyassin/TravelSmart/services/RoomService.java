package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Room;
import com.ahmedyassin.TravelSmart.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    @Transactional
    public Room create(Room r){
        return roomRepository.save(r);
    }

    @Transactional
    public Room update(UUID id, Room updates){
        Room existing = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        existing.setRoomNumber(updates.getRoomNumber());
        existing.setType(updates.getType());
        existing.setPrice(updates.getPrice());
        existing.setAvailable(updates.getAvailable());
        return roomRepository.save(existing);
    }

    public Optional<Room> findById(UUID id){
        return roomRepository.findById(id);
    }

    public List<Room> findAll(){
        return roomRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        roomRepository.deleteById(id);
    }
}

