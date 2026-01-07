package com.ahmedyassin.TravelSmart.repositories;

import com.ahmedyassin.TravelSmart.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByHotelId(UUID hotelId);
    List<Room> findByHotelIdAndIsAvailableTrue(UUID hotelId);
    Optional<Room> findByHotelIdAndRoomType(UUID hotelId, String roomType);
}

