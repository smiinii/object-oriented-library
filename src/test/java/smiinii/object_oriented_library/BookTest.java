package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.StoredBook;

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
}
