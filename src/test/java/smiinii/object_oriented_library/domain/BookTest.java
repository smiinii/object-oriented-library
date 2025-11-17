package smiinii.object_oriented_library.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;
import smiinii.object_oriented_library.domain.storedbook.StoredBookStatus;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookTest {

    @Test
    @DisplayName("registerNew: 정상적으로 책과 초기 소장본을 생성한다")
    void createsBookWithInitialCopies() {
        // given
        String title = "클린 코드";
        String author = "로버트 마틴";
        int initialCount = 3;
        // when
        Book book = Book.registerNew(title, author, initialCount);
        // then
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("클린 코드");
        assertThat(book.getAuthor()).isEqualTo("로버트 마틴");
        assertThat(book.getStoredBooks().size()).isEqualTo(3);
        assertThat(book.getStoredBooks().getStoredBooks())
                .allMatch(StoredBook::isAvailable);
    }

    @Test
    @DisplayName("registerNew: 제목/저자가 비어있으면 예외가 발생한다")
    void throwsWhenTitleOrAuthorBlank() {
        // given & when & then
        assertThatThrownBy(() -> Book.registerNew("", "저자", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("제목이 필요합니다.");

        assertThatThrownBy(() -> Book.registerNew("제목", "", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("저자가 필요합니다.");
    }

    @Test
    @DisplayName("registerNew: 초기 소장본 수가 0 이하이면 예외가 발생한다")
    void throwsWhenInitialCountNonPositive() {
        assertThatThrownBy(() -> Book.registerNew("제목", "저자", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 소장본 수는 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("addAvailableStoredBooks: 운영 중 소장본을 추가로 입고할 수 있다")
    void addsMoreCopies() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 1);

        // when
        book.addAvailableStoredBooks(2);

        // then
        assertThat(book.getStoredBooks().size()).isEqualTo(3);
    }

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2025-01-01T00:00:00Z"),
            ZoneId.of("UTC")
    );

    @Test
    @DisplayName("tryReserve: 가용 소장본이 하나라도 있으면 예약을 받지 않는다")
    void tryReserveFailsWhenAnyCopyIsAvailable() {
        // given
        Book book = Book.registerNew("자바의 정석", "남궁성", 1);
        // when
        boolean reserved = book.tryReserve(1L, 3, fixedClock);
        // then
        assertThat(reserved).isFalse();
    }

    @Test
    @DisplayName("tryReserve: 모든 소장본이 LOANED 상태일 때만 예약을 받는다")
    void tryReserveSucceedsWhenAllCopiesLoaned() {
        // given
        Book book = Book.registerNew("자바의 정석", "남궁성", 1);
        StoredBook storedBook = book.getStoredBooks().getStoredBooks().get(0);
        storedBook.loan();
        // when
        boolean reserved = book.tryReserve(1L, 3, fixedClock);
        // then
        assertThat(reserved).isTrue();
    }

    @Test
    @DisplayName("assignHoldIfReservationExists: 가용 소장본이 없으면 아무 것도 하지 않는다")
    void doesNothingWhenNoAvailableCopy() {
        // given
        Book book = Book.registerNew("자바의 정석", "남궁성", 1);
        StoredBook storedBook = book.getStoredBooks().getStoredBooks().get(0);
        storedBook.loan();
        book.tryReserve(1L, 3, fixedClock);
        // when
        book.assignHoldIfReservationExists(Duration.ofHours(3), fixedClock);
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.LOANED);
    }

    @Test
    @DisplayName("assignHoldIfReservationExists: 가용 소장본은 있어도 예약 대기열이 없으면 아무 것도 하지 않는다")
    void doesNothingWhenNoReservationQueued() {
        // given
        Book book = Book.registerNew("자바의 정석", "남궁성", 1);
        StoredBook storedBook = book.getStoredBooks().getStoredBooks().get(0);
        // when
        book.assignHoldIfReservationExists(Duration.ofHours(3), fixedClock);
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }

    @Test
    @DisplayName("assignHoldIfReservationExists: 가용 소장본과 예약 대기열이 모두 있으면 HOLD를 부여한다")
    void assignsHoldWhenAvailableCopyAndReservationExist() {
        // given
        Book book = Book.registerNew("자바의 정석", "남궁성", 1);
        StoredBook storedBook = book.getStoredBooks().getStoredBooks().get(0);
        storedBook.loan();

        boolean reserved = book.tryReserve(1L, 3, fixedClock);
        assertThat(reserved).isTrue();

        storedBook.returnToAvailable();
        // when
        book.assignHoldIfReservationExists(Duration.ofHours(3), fixedClock);
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.ON_HOLD);
    }
}
