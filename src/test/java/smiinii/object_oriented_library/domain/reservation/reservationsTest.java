package smiinii.object_oriented_library.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.reservation.Reservation;
import smiinii.object_oriented_library.domain.reservation.ReservationStatus;
import smiinii.object_oriented_library.domain.reservation.Reservations;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class reservationsTest {

    @Test
    @DisplayName("tryReserve : 같은 회원이 활성 예약을 가진 경우 false 반환")
    void notSameMemberOrnotIsActive() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 11, 12, 10, 0)
                .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        // when
        boolean first = reservations.tryReserve(book, 1L, 3, fixedClock);
        boolean second = reservations.tryReserve(book, 1L, 3, fixedClock);
        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }
    
    @Test
    @DisplayName("tryReserve : 예약 최대 대기열을 넘어갈 경우 false를 반환")
    void exceedsMaxQueueSize() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 11, 12, 10, 0)
                        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        int maxQueueSize = 1;
        // when
        boolean first = reservations.tryReserve(book, 1L, maxQueueSize, fixedClock);
        boolean second = reservations.tryReserve(book, 2L, maxQueueSize, fixedClock);
        // then
        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    @DisplayName("tryReserve : 예약 시도 성공")
    void successTryReserve() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 11, 12, 10, 0)
                        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        int maxQueueSize = 1;
        // when
        boolean result = reservations.tryReserve(book, 1L, maxQueueSize, fixedClock);
        // then
        assertThat(result).isTrue();
        assertThat(reservations.getReservations()).hasSize(1);
    }

    @Test
    @DisplayName("assignHoldToNextInQueue : 예약 대기자가 없으면 false 반환")
    void notQueued() {
        // given
        Reservations reservations = new Reservations();
        Duration duration = Duration.ofHours(3);
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 11, 12, 10, 0)
                        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
        // when
        boolean result = reservations.assignHoldToNextInQueue(10L, duration, fixedClock);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("assignHoldToNextInQueue : 예약 대기자가 있으면 우선권 부여하고 true 반환")
    void assignHoldToNextInQueue() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        Duration duration = Duration.ofHours(3);
        Clock fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 11, 12, 10, 0)
                        .toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

        reservations.tryReserve(book, 1L, 3, fixedClock); // 선두
        reservations.tryReserve(book, 2L, 3, fixedClock); // 2번째
        // when
        boolean result = reservations.assignHoldToNextInQueue(10L, duration, fixedClock);
        Reservation first = reservations.getReservations().get(0);
        Reservation second = reservations.getReservations().get(1);
        // then
        assertThat(result).isTrue();
        assertThat(first.getReservationStatus()).isEqualTo(ReservationStatus.HOLD_READY);
        assertThat(second.getReservationStatus()).isEqualTo(ReservationStatus.QUEUED);
    }

    @Test
    @DisplayName("releaseExpiredHolds: 기한 지난 HOLD_READY는 해제되고 EXPIRED로 전이")
    void releasesOverdueHoldAndTransitionsToExpired() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        reservations.tryReserve(book, 1L, 3, java.time.Clock.systemUTC());

        Reservation head = reservations.headQueued().orElseThrow(() ->
                new IllegalArgumentException("대기 중인 예약이 없습니다."));
        head.prepareHold(10L, LocalDateTime.of(2025, 11, 12, 11, 0));

        // when
        LocalDateTime now = LocalDateTime.of(2025, 11, 12, 11, 0, 1);
        List<Long> released = reservations.releaseExpiredHolds(now);

        // then
        assertThat(released).containsExactly(10L);
        assertThat(head.getReservationStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(head.getHoldStoredBookId()).isNull();
        assertThat(head.getHoldUntil()).isNull();
    }

    @Test
    @DisplayName("findCollectible: HOLD_READY이고 회원·소장본이 일치하면 Optional에 담겨 반환")
    void returnsCollectibleWhenHoldReadyAndMatches() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservations reservations = new Reservations();
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 12, 0);

        reservations.tryReserve(book, 1L, 3, java.time.Clock.systemUTC());
        Reservation head = reservations.headQueued().orElseThrow(() ->
                new IllegalArgumentException("대기 중인 예약이 없습니다."));
        head.prepareHold(10L, holdUntil);
        // when
        Reservation result = reservations.findCollectible(1L, 10L).orElseThrow(() ->
                new IllegalArgumentException("매칭 되는 예약자가 없습니다."));
        // then
        assertThat(result.getReservationStatus()).isEqualTo(ReservationStatus.HOLD_READY);
        assertThat(result.getMemberId()).isEqualTo(1L);
        assertThat(result.getHoldStoredBookId()).isEqualTo(10L);
    }
}
