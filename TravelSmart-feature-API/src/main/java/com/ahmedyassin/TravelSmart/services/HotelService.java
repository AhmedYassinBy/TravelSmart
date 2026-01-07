package com.ahmedyassin.TravelSmart.services;

import com.ahmedyassin.TravelSmart.entities.Hotel;
import com.ahmedyassin.TravelSmart.entities.Room;
import com.ahmedyassin.TravelSmart.repositories.HotelRepository;
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
public class HotelService {
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    // ===== ADMIN METHODS (Existing) =====

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
        // Update new fields
        if (updates.getPricePerNight() != null) existing.setPricePerNight(updates.getPricePerNight());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
        if (updates.getAmenities() != null) existing.setAmenities(updates.getAmenities());
        if (updates.getLatitude() != null) existing.setLatitude(updates.getLatitude());
        if (updates.getLongitude() != null) existing.setLongitude(updates.getLongitude());
        if (updates.getPhone() != null) existing.setPhone(updates.getPhone());
        if (updates.getEmail() != null) existing.setEmail(updates.getEmail());
        if (updates.getWebsite() != null) existing.setWebsite(updates.getWebsite());
        if (updates.getCheckInTime() != null) existing.setCheckInTime(updates.getCheckInTime());
        if (updates.getCheckOutTime() != null) existing.setCheckOutTime(updates.getCheckOutTime());
        if (updates.getCancellationPolicy() != null) existing.setCancellationPolicy(updates.getCancellationPolicy());
        if (updates.getIsActive() != null) existing.setIsActive(updates.getIsActive());
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

    // ===== MOBILE METHODS (New) =====

    /**
     * Get all active hotels for mobile app
     */
    public List<Hotel> getAllActiveHotels() {
        return hotelRepository.findByIsActiveTrue();
    }

    /**
     * Get hotel by ID (for mobile)
     */
    public Hotel getHotelById(UUID id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found: " + id));
    }

    /**
     * Get hotels by city (for mobile)
     */
    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByCityAndIsActiveTrue(city);
    }

    /**
     * Get hotels by star rating (for mobile)
     */
    public List<Hotel> getHotelsByStars(Integer etoile) {
        return hotelRepository.findByEtoile(etoile);
    }

    /**
     * Search hotels with filters (for mobile)
     */
    public List<Hotel> searchHotels(String destination, Double minPrice, Double maxPrice, String sortBy, String sortOrder) {
        List<Hotel> hotels = hotelRepository.findByIsActiveTrue();

        // Filter by destination (city)
        if (destination != null && !destination.isEmpty()) {
            hotels = hotels.stream()
                    .filter(h -> h.getCity() != null && h.getCity().toLowerCase().contains(destination.toLowerCase()))
                    .toList();
        }

        // Filter by min price
        if (minPrice != null) {
            hotels = hotels.stream()
                    .filter(h -> h.getPricePerNight() != null && h.getPricePerNight() >= minPrice)
                    .toList();
        }

        // Filter by max price
        if (maxPrice != null) {
            hotels = hotels.stream()
                    .filter(h -> h.getPricePerNight() != null && h.getPricePerNight() <= maxPrice)
                    .toList();
        }

        // Sort
        if ("price".equals(sortBy)) {
            if ("desc".equals(sortOrder)) {
                hotels = hotels.stream()
                        .sorted((h1, h2) -> {
                            Double p1 = h1.getPricePerNight() != null ? h1.getPricePerNight() : 0.0;
                            Double p2 = h2.getPricePerNight() != null ? h2.getPricePerNight() : 0.0;
                            return Double.compare(p2, p1);
                        })
                        .toList();
            } else {
                hotels = hotels.stream()
                        .sorted((h1, h2) -> {
                            Double p1 = h1.getPricePerNight() != null ? h1.getPricePerNight() : 0.0;
                            Double p2 = h2.getPricePerNight() != null ? h2.getPricePerNight() : 0.0;
                            return Double.compare(p1, p2);
                        })
                        .toList();
            }
        }

        return hotels;
    }
}
