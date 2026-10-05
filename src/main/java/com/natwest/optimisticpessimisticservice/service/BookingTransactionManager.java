package com.natwest.optimisticpessimisticservice.service;

import com.natwest.optimisticpessimisticservice.entity.Seat;
import com.natwest.optimisticpessimisticservice.repository.MovieTicketBookingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BookingTransactionManager {

    private final MovieTicketBookingRepository movieTicketBookingRepository;

    public BookingTransactionManager(MovieTicketBookingRepository movieTicketBookingRepository) {
        this.movieTicketBookingRepository = movieTicketBookingRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Seat executeOptimisticBooking(Long seatId) {
        Seat seat = movieTicketBookingRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with id " + seatId));

        if (seat.isBooked()) {
            throw new RuntimeException("Seat is already booked with id " + seatId);
        }
        seat.setBooked(true);
        return movieTicketBookingRepository.save(seat); // Triggers optimistic version comparison
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Seat executePessimisticBooking(Long seatId) {
        // Blocks thread right here if another transaction has a lock on this row
        Seat seat = movieTicketBookingRepository.findByIdAndLock(seatId)
                .orElseThrow(() -> new RuntimeException("Seat not found with id " + seatId));

        if (seat.isBooked()) {
            throw new RuntimeException("Seat is already booked with id " + seatId);
        }
        seat.setBooked(true);
        return movieTicketBookingRepository.save(seat);
    }
}
