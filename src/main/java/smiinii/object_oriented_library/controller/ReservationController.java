package smiinii.object_oriented_library.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smiinii.object_oriented_library.domain.reservation.Reservation;
import smiinii.object_oriented_library.dto.reservation.ReservationRequest;
import smiinii.object_oriented_library.dto.reservation.ReservationResponse;
import smiinii.object_oriented_library.service.ReservationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Void> reserve(@RequestBody ReservationRequest request) {
        reservationService.reserve(request.getMemberId(), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/books/{bookId}")
    public ResponseEntity<List<ReservationResponse>> getReservationsForBook(@PathVariable Long bookId) {
        List<Reservation> reservations = reservationService.getReservationsForBook(bookId);

        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
