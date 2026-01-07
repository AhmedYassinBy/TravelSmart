package com.ahmedyassin.TravelSmart.repositories;

import com.ahmedyassin.TravelSmart.entities.Reservation;
import com.ahmedyassin.TravelSmart.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    List<Reservation> findByClientEmailOrderByBookingDateDesc(String clientEmail);

    List<Reservation> findByClientEmail(String clientEmail);

    List<Reservation> findByOfferIdAndOfferType(UUID offerId, String offerType);

    List<Reservation> findByStatus(ReservationStatus status);
}
