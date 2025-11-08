package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.StoredBook;
import smiinii.object_oriented_library.domain.StoredBooks;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoredBooksTest {

    @Test
    @DisplayName("add() - null을 추가하면 IllegalArgumentException 발생")
    void storedBookIsNull() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        // when & then
        assertThatThrownBy(() -> storedBooks.add(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("소장본에 추가할 책이 비어있습니다.");
    }

    @Test
    @DisplayName("add() 호출 시 StoredBook이 리스트에 추가된다")
    void addStoredBooks() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook Book = StoredBook.createAvailable(1L);
        // when
        storedBooks.add(Book);
        // then
        assertThat(storedBooks.getStoredBooks()).hasSize(1).containsExactly(Book);
    }

    @Test
    @DisplayName("외부에서 반환 리스트를 수정해도 내부 상태는 변하지 않는다")
    void returnedListIsImmutable() {
        StoredBooks storedBooks = new StoredBooks();
        StoredBook book = StoredBook.createAvailable(1L);
        storedBooks.add(book);

        List<StoredBook> result = storedBooks.getStoredBooks();

        assertThatThrownBy(() -> result.add(StoredBook.createAvailable(2L)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("모든 소장본이 대출 중이면 allLoaned()는 true를 반환한다.")
    void allLoanedTrueWhenAllBooksLoaned() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook book = StoredBook.createAvailable(1L);
        book.loan(); // 상태 전이 AVAILABLE → LOANED
        // when
        storedBooks.add(book);
        // then
        assertThat(storedBooks.allLoaned()).isTrue();
    }

    @Test
    @DisplayName("소장본이 비어있으면 예외처리")
    void storedBooksIsNull() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        // when & then
        assertThatThrownBy(storedBooks::allLoaned)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("소장본이 비어있습니다.");
    }
}
