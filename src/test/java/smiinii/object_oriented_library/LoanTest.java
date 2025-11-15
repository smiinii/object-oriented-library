package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.Loan;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;
import smiinii.object_oriented_library.domain.storedbook.StoredBookStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LoanTest {

    @Test
    @DisplayName("of : 대출 생성 성공")
    void createLoanSuccessfully() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);

        LocalDateTime loanedAt = LocalDateTime.of(2025,1,1,10,0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        // when
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);
        //then
        assertThat(loan.getMember()).isEqualTo(member);
        assertThat(loan.getStoredBook()).isEqualTo(storedBook);
        assertThat(loan.getLoanedAt()).isEqualTo(loanedAt);
        assertThat(loan.getDueDate()).isEqualTo(dueDate);
        assertThat(loan.isActive()).isTrue();
    }

    @Test
    @DisplayName("returnBook : 예약 없음 → 소장본을 AVAILABLE로 변경한다")
    void withoutReservation_changesStoredBookToAvailable() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);
        LocalDateTime now = loanedAt.plusDays(3);
        // when
        loan.returnBook(now, false);
        // then
        assertThat(loan.isActive()).isFalse();
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }

    @Test
    @DisplayName("returnBook : 예약 있음 → 소장본을 ON_HOLD로 변경한다")
    void withReservation_changesStoredBookToOnHold() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);
        LocalDateTime now = loanedAt.plusDays(3);
        // when
        loan.returnBook(now, true);
        // then
        assertThat(loan.isActive()).isFalse();
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.ON_HOLD);
    }

    @Test
    @DisplayName("returnBook : 이미 반납된 대출은 다시 반납할 수 없다")
    void returnBookFailWhenAlreadyReturned() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);

        LocalDateTime firstReturn = loanedAt.plusDays(3);
        loan.returnBook(firstReturn, false);

        LocalDateTime secondReturn = firstReturn.plusHours(1);

        // when & then
        assertThatThrownBy(() -> loan.returnBook(secondReturn, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("대출 중인 도서만 반납할 수 있습니다.");
    }

    @Test
    @DisplayName("extend : 대출 중이고, 연체 아니며, 예약이 없으면 연장된다")
    void extendSuccess() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);

        LocalDateTime now = loanedAt.plusDays(3);
        LocalDateTime newDueDate = dueDate.plusDays(7);
        // when
        loan.extend(now, false, newDueDate);
        // then
        assertThat(loan.getDueDate()).isEqualTo(newDueDate);
    }

    @Test
    @DisplayName("extend : 이미 반납된 도서는 연장할 수 없다")
    void extendFailWhenReturned() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);

        LocalDateTime returnTime = loanedAt.plusDays(3);
        loan.returnBook(returnTime, false);

        LocalDateTime now = returnTime.plusHours(1);
        LocalDateTime newDueDate = dueDate.plusDays(7);
        // when & then
        assertThatThrownBy(() -> loan.extend(now, false, newDueDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대출 중인 도서만 연장할 수 있습니다.");
    }

    @Test
    @DisplayName("extend : 연체 중인 도서는 연장할 수 없다")
    void extendFailWhenOverdue() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);

        LocalDateTime now = dueDate.plusSeconds(1);
        LocalDateTime newDueDate = dueDate.plusDays(7);
        // when & then
        assertThatThrownBy(() -> loan.extend(now, false, newDueDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("연체 중인 도서는 연장할 수 없습니다.");
    }

    @Test
    @DisplayName("extend : 다른 회원의 예약 대기열이 존재하면 연장할 수 없다")
    void extendFailWhenReservationExists() {
        // given
        Member member = Member.generation("smini");
        Book book = Book.registerNew("자바의정석", "남궁성", 1);
        StoredBook storedBook = StoredBook.createAvailable(book);
        storedBook.loan();

        LocalDateTime loanedAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime dueDate = loanedAt.plusDays(7);
        Loan loan = Loan.of(member, storedBook, loanedAt, dueDate);

        LocalDateTime now = loanedAt.plusDays(3);
        LocalDateTime newDueDate = dueDate.plusDays(7);
        // when & then
        assertThatThrownBy(() -> loan.extend(now, true, newDueDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("다른 회원의 예약 대기열이 존재해 연장할 수 없습니다.");
    }
}
