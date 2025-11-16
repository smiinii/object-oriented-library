package smiinii.object_oriented_library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smiinii.object_oriented_library.domain.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
