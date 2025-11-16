package com.ahmedyassin.TravelSmart.controllers;

import com.ahmedyassin.TravelSmart.entities.*;
import com.ahmedyassin.TravelSmart.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/offers")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferController {
    private final FlightService flightService;
    private final HotelService hotelService;
    private final RoomService roomService;
    private final CircuitService circuitService;
    private final ActivityService activityService;

    // Flights
    @PostMapping("/flights")
    public ResponseEntity<Flight> createFlight(@RequestBody Flight f){
        return ResponseEntity.ok(flightService.create(f));
    }

    @GetMapping("/flights")
    public ResponseEntity<List<Flight>> listFlights(){
        return ResponseEntity.ok(flightService.findAll());
    }

    @PutMapping("/flights/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable UUID id, @RequestBody Flight f){
        return ResponseEntity.ok(flightService.update(id,f));
    }

    @DeleteMapping("/flights/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable UUID id){
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Hotels
    @PostMapping("/hotels")
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel h){
        return ResponseEntity.ok(hotelService.create(h));
    }

    @GetMapping("/hotels")
    public ResponseEntity<List<Hotel>> listHotels(){
        return ResponseEntity.ok(hotelService.findAll());
    }

    @PutMapping("/hotels/{id}")
    public ResponseEntity<Hotel> updateHotel(@PathVariable UUID id, @RequestBody Hotel h){
        return ResponseEntity.ok(hotelService.update(id,h));
    }

    @DeleteMapping("/hotels/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable UUID id){
        hotelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Rooms
    @PostMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<Room> addRoom(@PathVariable UUID hotelId, @RequestBody Room r){
        return ResponseEntity.ok(hotelService.addRoom(hotelId,r));
    }

    @GetMapping("/hotels/{hotelId}/rooms")
    public ResponseEntity<List<Room>> listHotelRooms(@PathVariable UUID hotelId){
        return ResponseEntity.ok(hotelService.getRooms(hotelId));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable UUID id, @RequestBody Room r){
        return ResponseEntity.ok(roomService.update(id,r));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable UUID id){
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Circuits
    @PostMapping("/circuits")
    public ResponseEntity<Circuit> createCircuit(@RequestBody Circuit c){
        return ResponseEntity.ok(circuitService.create(c));
    }

    @GetMapping("/circuits")
    public ResponseEntity<List<Circuit>> listCircuits(){
        return ResponseEntity.ok(circuitService.findAll());
    }

    @PutMapping("/circuits/{id}")
    public ResponseEntity<Circuit> updateCircuit(@PathVariable UUID id, @RequestBody Circuit c){
        return ResponseEntity.ok(circuitService.update(id,c));
    }

    @DeleteMapping("/circuits/{id}")
    public ResponseEntity<Void> deleteCircuit(@PathVariable UUID id){
        circuitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/circuits/{circuitId}/hotels/{hotelId}")
    public ResponseEntity<Circuit> addHotelToCircuit(@PathVariable UUID circuitId, @PathVariable UUID hotelId){
        return ResponseEntity.ok(circuitService.addHotel(circuitId, hotelId));
    }

    @PostMapping("/circuits/{circuitId}/activities/{activityId}")
    public ResponseEntity<Circuit> addActivityToCircuit(@PathVariable UUID circuitId, @PathVariable UUID activityId){
        return ResponseEntity.ok(circuitService.addActivity(circuitId, activityId));
    }

    // Activities
    @PostMapping("/activities")
    public ResponseEntity<Activity> createActivity(@RequestBody Activity a){
        return ResponseEntity.ok(activityService.create(a));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<Activity>> listActivities(){
        return ResponseEntity.ok(activityService.findAll());
    }

    @PutMapping("/activities/{id}")
    public ResponseEntity<Activity> updateActivity(@PathVariable UUID id, @RequestBody Activity a){
        return ResponseEntity.ok(activityService.update(id,a));
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable UUID id){
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
