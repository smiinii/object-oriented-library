package smiinii.object_oriented_library.domain.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class reservationTest {

    @Test
    @DisplayName("create: 예약 생성")
    void reservationCreate() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Long memberId = 1L;
        LocalDateTime createdAt = LocalDateTime.of(2025, 11, 11, 21, 0);
        // when
        Reservation reservation = Reservation.create(book, memberId, createdAt);
        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getBook()).isEqualTo(book);
        assertThat(reservation.getMemberId()).isEqualTo(memberId);
        assertThat(reservation.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("prepareHold: 예약 대기열이 아니면 예외")
    void throwsWhenNotQueued() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        reservation.prepareHold(10L, LocalDateTime.now());
        // when & then
        assertThatThrownBy(() -> reservation.prepareHold(11L, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약 대기열에 존재하지 않습니다.");
    }

    @Test
    @DisplayName("prepareHold: QUEUED이면 HOLD_READY로 전이하고 필드 세팅")
    void transitionsAndSetsFields() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        LocalDateTime created = LocalDateTime.of(2025, 11, 12, 8, 0);
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 9, 0);
        Reservation reservation = Reservation.create(book, 1L, created);
        // when
        reservation.prepareHold(10L, holdUntil);
        // then
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.HOLD_READY);
        assertThat(reservation.getCreatedAt()).isEqualTo(created);
        assertThat(reservation.getHoldUntil()).isEqualTo(holdUntil);
        assertThat(reservation.getHoldStoredBookId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("expire: 만료시각 이후면 EXPIRED로 전이하고 hold 해제")
    void afterHoldUntilExpires() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        LocalDateTime created = LocalDateTime.of(2025, 11, 12, 8, 0);
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 9, 0);

        Reservation reservation = Reservation.create(book, 1L, created);
        reservation.prepareHold(10L, holdUntil);
        // when
        reservation.expireIfOverdue(holdUntil.plusSeconds(1));
        // then
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.EXPIRED);
        assertThat(reservation.getHoldStoredBookId()).isNull();
    }

    @Test
    @DisplayName("expire: 만료시각 이전이면 만료되지 않음")
    void beforeHoldUntilDoesNotExpire() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        LocalDateTime created = LocalDateTime.of(2025, 11, 12, 8, 0);
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 9, 0);

        Reservation reservation = Reservation.create(book, 1L, created);
        reservation.prepareHold(10L, holdUntil);
        // when
        reservation.expireIfOverdue(holdUntil.minusSeconds(1));
        // then
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.HOLD_READY);
        assertThat(reservation.getHoldStoredBookId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("complete: HOLD_READY가 아니면 예외")
    void throwsWhenNotHoldReady() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        LocalDateTime created = LocalDateTime.of(2025, 11, 12, 8, 0);
        Reservation reservation = Reservation.create(book, 1L, created);
        // when & then
        assertThatThrownBy(() -> reservation.complete(LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대출 대기 상태가 아닙니다.");
    }

    @Test
    @DisplayName("complete: now가 holdUntil 이후면 예외")
    void throwsWhenAfterHoldUntil() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 9, 0);
        reservation.prepareHold(10L, holdUntil);
        // when & then
        assertThatThrownBy(() -> reservation.complete(holdUntil.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대출 대기 기한이 만료되었습니다.");
    }

    @Test
    @DisplayName("complete: 만료 전이면 LOAN_COMPLETE로 전이")
    void beforeHoldUntilTransitionsToLoanComplete() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        LocalDateTime holdUntil = LocalDateTime.of(2025, 11, 12, 9, 0);
        reservation.prepareHold(10L, holdUntil);
        // when
        reservation.complete(holdUntil.minusSeconds(1));
        // then
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.LOAN_COMPLETE);
    }

    @Test
    @DisplayName("sameMember: 같은 회원이면 true")
    void returnsTrueForSameMember() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        // when
        boolean result = reservation.sameMember(1L);
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("sameMember: 다른 회원이면 false")
    void returnsFalseForDifferentMember() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        // when
        boolean result = reservation.sameMember(2L);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("matchesHold: HOLD_READY이고 동일 storedBookId면 true")
    void returnsTrueWhenHoldReadyAndSameId() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        reservation.prepareHold(10L, LocalDateTime.now().plusDays(1));
        // when
        boolean result = reservation.matchesHold(10L);
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("matchesHold: HOLD_READY여도 storedBookId 다르면 false")
    void returnsFalseWhenHoldReadyButDifferentId() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        reservation.prepareHold(10L, LocalDateTime.now().plusDays(1));
        // when
        boolean result = reservation.matchesHold(11L);
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("matchesHold: HOLD_READY가 아니면 false")
    void returnsFalseWhenNotHoldReady() {
        // given (아직 QUEUED)
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 3);
        Reservation reservation = Reservation.create(book, 1L, LocalDateTime.now());
        // when
        boolean result = reservation.matchesHold(10L);
        // then
        assertThat(result).isFalse();
    }
}
