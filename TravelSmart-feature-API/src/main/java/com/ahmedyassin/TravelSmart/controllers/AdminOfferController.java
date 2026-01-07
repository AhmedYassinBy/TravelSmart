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

    // Circuits CRUD
    @PostMapping("/circuits")
    public ResponseEntity<Circuit> createCircuit(@RequestBody Circuit c){
        return ResponseEntity.ok(circuitService.create(c));
    }

    @GetMapping("/circuits")
    public ResponseEntity<List<Circuit>> listCircuits(){
        return ResponseEntity.ok(circuitService.findAll());
    }

    @GetMapping("/circuits/{id}")
    public ResponseEntity<Circuit> getCircuit(@PathVariable UUID id){
        return circuitService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    // Circuit - Etapes (Steps)
    @GetMapping("/circuits/{circuitId}/etapes")
    public ResponseEntity<List<Etape>> getCircuitEtapes(@PathVariable UUID circuitId){
        return ResponseEntity.ok(circuitService.getEtapes(circuitId));
    }

    @PostMapping("/circuits/{circuitId}/etapes")
    public ResponseEntity<Circuit> addEtapeToCircuit(@PathVariable UUID circuitId, @RequestBody Etape etape){
        return ResponseEntity.ok(circuitService.addEtape(circuitId, etape));
    }

    @PutMapping("/circuits/{circuitId}/etapes/{etapeId}")
    public ResponseEntity<Circuit> updateEtape(@PathVariable UUID circuitId, @PathVariable UUID etapeId, @RequestBody Etape etape){
        return ResponseEntity.ok(circuitService.updateEtape(circuitId, etapeId, etape));
    }

    @DeleteMapping("/circuits/{circuitId}/etapes/{etapeId}")
    public ResponseEntity<Circuit> removeEtapeFromCircuit(@PathVariable UUID circuitId, @PathVariable UUID etapeId){
        return ResponseEntity.ok(circuitService.removeEtape(circuitId, etapeId));
    }

    // Circuit - Hotels Association
    @PostMapping("/circuits/{circuitId}/hotels/{hotelId}")
    public ResponseEntity<Circuit> addHotelToCircuit(@PathVariable UUID circuitId, @PathVariable UUID hotelId){
        return ResponseEntity.ok(circuitService.addHotel(circuitId, hotelId));
    }

    @DeleteMapping("/circuits/{circuitId}/hotels/{hotelId}")
    public ResponseEntity<Circuit> removeHotelFromCircuit(@PathVariable UUID circuitId, @PathVariable UUID hotelId){
        return ResponseEntity.ok(circuitService.removeHotel(circuitId, hotelId));
    }

    @PutMapping("/circuits/{circuitId}/hotels")
    public ResponseEntity<Circuit> setCircuitHotels(@PathVariable UUID circuitId, @RequestBody List<UUID> hotelIds){
        return ResponseEntity.ok(circuitService.setHotels(circuitId, hotelIds));
    }

    // Circuit - Activities Association
    @PostMapping("/circuits/{circuitId}/activities/{activityId}")
    public ResponseEntity<Circuit> addActivityToCircuit(@PathVariable UUID circuitId, @PathVariable UUID activityId){
        return ResponseEntity.ok(circuitService.addActivity(circuitId, activityId));
    }

    @DeleteMapping("/circuits/{circuitId}/activities/{activityId}")
    public ResponseEntity<Circuit> removeActivityFromCircuit(@PathVariable UUID circuitId, @PathVariable UUID activityId){
        return ResponseEntity.ok(circuitService.removeActivity(circuitId, activityId));
    }

    @PutMapping("/circuits/{circuitId}/activities")
    public ResponseEntity<Circuit> setCircuitActivities(@PathVariable UUID circuitId, @RequestBody List<UUID> activityIds){
        return ResponseEntity.ok(circuitService.setActivities(circuitId, activityIds));
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
