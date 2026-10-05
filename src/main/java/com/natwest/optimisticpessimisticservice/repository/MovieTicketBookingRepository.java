package com.natwest.optimisticpessimisticservice.repository;

import com.natwest.optimisticpessimisticservice.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieTicketBookingRepository extends JpaRepository<Seat, Long> {

    // Triggers "SELECT ... FOR UPDATE" in MySQL to block concurrent reads/writes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :seatId")
    Optional<Seat> findByIdAndLock(@Param("seatId") Long seatId);
}
