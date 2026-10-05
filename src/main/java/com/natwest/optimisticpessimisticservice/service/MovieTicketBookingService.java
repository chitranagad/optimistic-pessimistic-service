package com.natwest.optimisticpessimisticservice.service;

import com.natwest.optimisticpessimisticservice.entity.Seat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class MovieTicketBookingService {

    private final BookingTransactionManager transactionManager;

    public MovieTicketBookingService(BookingTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Seat bookSeatWithOptimistic(Long seatId) throws InterruptedException {
        AtomicReference<Seat> bookedSeat = new AtomicReference<>();

        Thread t1 = new Thread(() -> runOptimisticTask(seatId, bookedSeat), "Optimistic-Thread-1");
        Thread t2 = new Thread(() -> runOptimisticTask(seatId, bookedSeat), "Optimistic-Thread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (bookedSeat.get() == null) {
            throw new RuntimeException("Both threads failed to book the seat via Optimistic Locking.");
        }
        return bookedSeat.get();
    }

    public Seat bookSeatWithPessimistic(Long seatId) throws InterruptedException {
        AtomicReference<Seat> bookedSeat = new AtomicReference<>();

        Thread t1 = new Thread(() -> runPessimisticTask(seatId, bookedSeat), "Pessimistic-Thread-1");
        Thread t2 = new Thread(() -> runPessimisticTask(seatId, bookedSeat), "Pessimistic-Thread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (bookedSeat.get() == null) {
            throw new RuntimeException("Both threads failed to book the seat via Pessimistic Locking.");
        }
        return bookedSeat.get();
    }

    private void runOptimisticTask(Long seatId, AtomicReference<Seat> bookedSeat) {
        try {
            log.info("{} is trying to fetch seat ID: {}", Thread.currentThread().getName(), seatId);
            Seat seat = transactionManager.executeOptimisticBooking(seatId);
            bookedSeat.compareAndSet(null, seat);
            log.info("{} successfully completed booking for seat ID: {}", Thread.currentThread().getName(), seatId);
        } catch (Exception e) {
            log.error("{} failed booking path for seat ID: {}. Error: {}", Thread.currentThread().getName(), seatId, e.getMessage());
        }
    }

    private void runPessimisticTask(Long seatId, AtomicReference<Seat> bookedSeat) {
        try {
            log.info("{} is trying to acquire lock on seat ID: {}", Thread.currentThread().getName(), seatId);
            Seat seat = transactionManager.executePessimisticBooking(seatId);
            bookedSeat.compareAndSet(null, seat);
            log.info("{} successfully locked and booked seat ID: {}", Thread.currentThread().getName(), seatId);
        } catch (Exception e) {
            log.error("{} failed booking path for seat ID: {}. Error: {}", Thread.currentThread().getName(), seatId, e.getMessage());
        }
    }
}
