package smiinii.object_oriented_library.domain.storedbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoredBookTest {

    @Test
    @DisplayName("AVAILABLE이면 대출 성공")
    void loanSuccessTest() {
        // given
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        // when
        storedBook.loan();
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.LOANED);
    }

    @Test
    @DisplayName("이미 대출 중이면 예외처리")
    void loanFailedWhenAlreadyLoaned() {
        // given
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        storedBook.loan();
        // when & then
        assertThatThrownBy(storedBook::loan).isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("다른 회원이 예약 중이면 예외처리")
    void loanFailedWhenOnHold() {
        // given
        StoredBook storedBook = StoredBook.createOnHold(dummyBook());
        // when & then
        assertThatThrownBy(storedBook::loan).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("반납할 도서가 대출 중인 도서가 아니면 예외처리")
    void notLoanBook() {
        // given
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        // when & then
        assertThatThrownBy(() -> storedBook.returnBook(false)).isInstanceOf(IllegalStateException.class)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대출 중인 도서만 반납할 수 있습니다.");
    }

    @Test
    @DisplayName("반납 후 예약자가 있으면 ON_HOLD")
    void returnBookOnHold() {
        // given
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        // when
        storedBook.loan();
        storedBook.returnBook(true);
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.ON_HOLD);
    }

    @Test
    @DisplayName("반납 후 예약자가 없으면 AVAILABLE")
    void returnBookAvailable() {
        // given
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        // when
        storedBook.loan();
        storedBook.returnBook(false);
        // then
        assertThat(storedBook.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }

    private Book dummyBook() {
        return Book.registerNew("테스트 제목", "테스트 저자", 1);
    }
}
