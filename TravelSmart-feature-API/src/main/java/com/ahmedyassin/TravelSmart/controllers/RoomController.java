package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.Room;
import com.ahmedyassin.TravelSmart.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * Get available rooms for a hotel
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Room>> getRoomsByHotelId(@PathVariable UUID hotelId) {
        log.info("GET /api/rooms/hotel/{}", hotelId);
        List<Room> rooms = roomService.getRoomsByHotelId(hotelId);
        log.info("✅ {} room(s) found", rooms.size());
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get room by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable UUID id) {
        log.info("GET /api/rooms/{}", id);
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    /**
     * Create a new room
     */
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        log.info("POST /api/rooms");
        Room createdRoom = roomService.createRoom(room);
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * Update a room
     */
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID id, @RequestBody Room room) {
        log.info("PUT /api/rooms/{}", id);
        Room updatedRoom = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updatedRoom);
    }

    /**
     * Delete (soft) a room
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id) {
        log.info("DELETE /api/rooms/{}", id);
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
