package com.ahmedyassin.TravelSmart.repositories;

import com.ahmedyassin.TravelSmart.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByClientEmail(String clientEmail);
    List<Payment> findByReservationId(UUID reservationId);
}
