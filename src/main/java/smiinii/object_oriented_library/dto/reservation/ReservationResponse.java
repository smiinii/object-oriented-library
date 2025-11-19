package smiinii.object_oriented_library.dto.reservation;

import smiinii.object_oriented_library.domain.reservation.Reservation;

import java.time.LocalDateTime;

public class ReservationResponse {

    private final Long reservationId;
    private final Long memberId;
    private final Long bookId;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime holdUntil;

    public ReservationResponse(Long reservationId,
                                      Long memberId,
                                      Long bookId,
                                      String status,
                                      LocalDateTime createdAt,
                                      LocalDateTime holdUntil) {
        this.reservationId = reservationId;
        this.memberId = memberId;
        this.bookId = bookId;
        this.status = status;
        this.createdAt = createdAt;
        this.holdUntil = holdUntil;
    }

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getBook().getId(),
                reservation.getReservationStatus().name(),
                reservation.getCreatedAt(),
                reservation.getHoldUntil()
        );
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }
}
