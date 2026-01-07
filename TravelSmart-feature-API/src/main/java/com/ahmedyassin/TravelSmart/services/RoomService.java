package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Room;
import com.ahmedyassin.TravelSmart.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    // ===== ADMIN METHODS (Existing) =====

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
        // Update new fields
        if (updates.getRoomType() != null) existing.setRoomType(updates.getRoomType());
        if (updates.getMaxOccupancy() != null) existing.setMaxOccupancy(updates.getMaxOccupancy());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getViewType() != null) existing.setViewType(updates.getViewType());
        if (updates.getBedType() != null) existing.setBedType(updates.getBedType());
        if (updates.getSizeSqm() != null) existing.setSizeSqm(updates.getSizeSqm());
        if (updates.getIsAvailable() != null) existing.setIsAvailable(updates.getIsAvailable());
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

    // ===== MOBILE METHODS (New) =====

    /**
     * Get available rooms for a hotel (for mobile)
     */
    public List<Room> getRoomsByHotelId(UUID hotelId) {
        return roomRepository.findByHotelIdAndIsAvailableTrue(hotelId);
    }

    /**
     * Get all rooms for a hotel including unavailable (for mobile)
     */
    public List<Room> getAllRoomsByHotelId(UUID hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    /**
     * Get room by ID (for mobile)
     */
    public Room getRoomById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found: " + id));
    }

    /**
     * Create room (for mobile)
     */
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    /**
     * Update room (for mobile)
     */
    public Room updateRoom(UUID id, Room roomDetails) {
        Room room = getRoomById(id);

        if (roomDetails.getRoomNumber() != null) room.setRoomNumber(roomDetails.getRoomNumber());
        if (roomDetails.getRoomType() != null) room.setRoomType(roomDetails.getRoomType());
        if (roomDetails.getPrice() != null) room.setPrice(roomDetails.getPrice());
        if (roomDetails.getMaxOccupancy() != null) room.setMaxOccupancy(roomDetails.getMaxOccupancy());
        if (roomDetails.getDescription() != null) room.setDescription(roomDetails.getDescription());
        if (roomDetails.getViewType() != null) room.setViewType(roomDetails.getViewType());
        if (roomDetails.getBedType() != null) room.setBedType(roomDetails.getBedType());
        if (roomDetails.getSizeSqm() != null) room.setSizeSqm(roomDetails.getSizeSqm());
        if (roomDetails.getIsAvailable() != null) room.setIsAvailable(roomDetails.getIsAvailable());

        return roomRepository.save(room);
    }

    /**
     * Soft delete room (for mobile)
     */
    public void deleteRoom(UUID id) {
        Room room = getRoomById(id);
        room.setIsAvailable(false);
        roomRepository.save(room);
    }
}

