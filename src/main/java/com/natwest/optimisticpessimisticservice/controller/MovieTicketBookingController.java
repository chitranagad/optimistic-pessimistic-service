package com.natwest.optimisticpessimisticservice.controller;

import com.natwest.optimisticpessimisticservice.entity.Seat;
import com.natwest.optimisticpessimisticservice.service.MovieTicketBookingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/booking")
public class MovieTicketBookingController {

    private final MovieTicketBookingService movieTicketBookingService;
    public MovieTicketBookingController(MovieTicketBookingService movieTicketBookingService) {
        this.movieTicketBookingService = movieTicketBookingService;
    }
    @GetMapping("/optimistic/{seatId}")
    public Seat optimisticBookTicket(@PathVariable Long seatId) throws InterruptedException {
        return movieTicketBookingService.bookSeatWithOptimistic(seatId);
    }

    @GetMapping("/pessimistic/{seatId}")
    public Seat pessimisticBookTicket(@PathVariable Long seatId) throws InterruptedException {
        return movieTicketBookingService.bookSeatWithPessimistic(seatId);
    }
}
