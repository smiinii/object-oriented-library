package smiinii.object_oriented_library.domain.reservation;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.Book;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Embeddable
public class Reservations {

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("createdAt ASC, id ASC")
    private List<Reservation> reservations = new ArrayList<>();

    public boolean tryReserve(Book book, Long memberId, int maxQueueSize, Clock clock) {
        if (reservations.stream().anyMatch(r ->
                r.sameMember(memberId) && r.isActive())) {
            return false;
        }
        long activeCount = reservations.stream().filter(Reservation::isActive).count();
        if (activeCount >= maxQueueSize) {
            return false;
        }
        reservations.add(Reservation.create(book, memberId, LocalDateTime.now(clock)));
        return true;
    }

    public boolean assignHoldToNextInQueue(Long storedBookId, Duration holdDuration, Clock clock) {
        Optional<Reservation> headReservation = headQueued();

        if (headReservation.isEmpty()) {
            return false;
        }

        Instant now = Instant.now(clock);
        Instant holdEnd = now.plus(holdDuration);
        LocalDateTime holdUntil = LocalDateTime.ofInstant(holdEnd, ZoneId.systemDefault());

        headReservation.get().prepareHold(storedBookId, holdUntil);
        return true;
    }

    public List<Long> releaseExpiredHolds(LocalDateTime now) {
        List<Long> expireStoredBookIds = new ArrayList<>();
        for (Reservation r : reservations) {
            r.expireIfOverdue(now).ifPresent(expireStoredBookIds::add);
        }
        return expireStoredBookIds;
    }

    public Optional<Reservation> findCollectible(Long memberId, Long storedBookId) {
        return reservations.stream()
                .filter(Reservation::isHoldReady)
                .filter(r -> r.sameMember(memberId) && r.matchesHold(storedBookId))
                .findFirst();
    }

    public Optional<Reservation> assignHoldToHeadIfExists(Long storedBookId, Duration holdDuration, Clock clock) {
        Optional<Reservation> headReservation = headQueued();
        if (headReservation.isEmpty()) {
            return Optional.empty();
        }

        Reservation reservation = headReservation.get();

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime holdUntil = now.plus(holdDuration);

        reservation.prepareHold(storedBookId, holdUntil);
        return Optional.of(reservation);
    }

    public Optional<Reservation> headQueued() {
        return reservations.stream().filter(Reservation::isQueued).findFirst();
    }

    public List<Reservation> getReservations() {
        return List.copyOf(reservations);
    }
}
