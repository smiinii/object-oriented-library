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
        assertThatThrownBy(() -> book.loan()).isInstanceOf(IllegalStateException.class);
    }
    
    @Test
    @DisplayName("다른 회원이 예약 중이면 예외처리")
    void loanFailedWhenOnHold() {
        // given
        StoredBook book = StoredBook.createOnHold(1L);
        // when & then
        assertThatThrownBy(() -> book.loan()).isInstanceOf(IllegalStateException.class);
    }
}
