package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.StoredBook;
import smiinii.object_oriented_library.domain.StoredBookStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoredBookTest {

    @Test
    @DisplayName("AVAILABLE이면 대출 성공")
    void loanSuccessTest() {
        // given
        StoredBook book = StoredBook.createAvailable(1L);
        // when
        book.loan();
        // then
        assertThat(book.getStatus()).isEqualTo(StoredBookStatus.LOANED);
    }

    @Test
    @DisplayName("이미 대출 중이면 예외처리")
    void loanFailedWhenAlreadyLoaned() {
        // given
        StoredBook book = StoredBook.createAvailable(1L);
        book.loan();
        // when & then
        assertThatThrownBy(book::loan).isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("다른 회원이 예약 중이면 예외처리")
    void loanFailedWhenOnHold() {
        // given
        StoredBook book = StoredBook.createOnHold(1L);
        // when & then
        assertThatThrownBy(book::loan).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("반납할 도서가 대출 중인 도서가 아니면 예외처리")
    void notLoanBook() {
        // given
        StoredBook book = StoredBook.createAvailable(1L);
        // when & then
        assertThatThrownBy(() -> book.returnBook(false)).isInstanceOf(IllegalStateException.class)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대출 중인 도서만 반납할 수 있습니다.");
    }

    @Test
    @DisplayName("반납 후 예약자가 있으면 ON_HOLD")
    void returnBookOnHold() {
        // given
        StoredBook book = StoredBook.createAvailable(1L);
        // when
        book.loan();
        book.returnBook(true);
        // then
        assertThat(book.getStatus()).isEqualTo(StoredBookStatus.ON_HOLD);
    }

    @Test
    @DisplayName("반납 후 예약자가 없으면 AVAILABLE")
    void returnBookAvailable() {
        // given
        StoredBook book = StoredBook.createAvailable(1L);
        // when
        book.loan();
        book.returnBook(false);
        // then
        assertThat(book.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }
}
