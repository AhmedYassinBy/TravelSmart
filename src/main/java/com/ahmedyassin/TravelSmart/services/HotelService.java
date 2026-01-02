package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Hotel;
import com.ahmedyassin.TravelSmart.entities.Room;
import com.ahmedyassin.TravelSmart.repositories.HotelRepository;
import com.ahmedyassin.TravelSmart.repositories.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public Hotel create(Hotel hotel){
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel update(UUID id, Hotel updates){
        Hotel existing = hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel not found"));
        existing.setName(updates.getName());
        existing.setAddress(updates.getAddress());
        existing.setCity(updates.getCity());
        existing.setCountry(updates.getCountry());
        existing.setEtoile(updates.getEtoile());
        return hotelRepository.save(existing);
    }

    public Optional<Hotel> findById(UUID id){
        return hotelRepository.findById(id);
    }

    public List<Hotel> findAll(){
        return hotelRepository.findAll();
    }

    @Transactional
    public void delete(UUID id){
        hotelRepository.deleteById(id);
    }

    @Transactional
    public Room addRoom(UUID hotelId, Room room){
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new RuntimeException("Hotel not found"));
        room.setHotel(hotel);
        Room saved = roomRepository.save(room);
        hotel.getRooms().add(saved);
        hotelRepository.save(hotel);
        return saved;
    }

    public List<Room> getRooms(UUID hotelId){
        return roomRepository.findByHotelId(hotelId);
    }
}
