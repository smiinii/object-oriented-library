package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.StoredBook;
import smiinii.object_oriented_library.domain.StoredBookStatus;
import smiinii.object_oriented_library.domain.StoredBooks;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

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
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        // when
        storedBooks.add(storedBook);
        // then
        assertThat(storedBooks.getStoredBooks()).hasSize(1).containsExactly(storedBook);
    }

    @Test
    @DisplayName("외부에서 반환 리스트를 수정해도 내부 상태는 변하지 않는다")
    void returnedListIsImmutable() {
        StoredBooks storedBooks = new StoredBooks();
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        storedBooks.add(storedBook);

        List<StoredBook> result = storedBooks.getStoredBooks();

        assertThatThrownBy(() -> result.add(StoredBook.createAvailable(dummyBook())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("모든 소장본이 대출 중이면 allLoaned()는 true를 반환한다.")
    void allLoanedTrueWhenAllBooksLoaned() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook storedBook = StoredBook.createAvailable(dummyBook());
        storedBook.loan(); // 상태 전이 AVAILABLE → LOANED
        // when
        storedBooks.add(storedBook);
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

    @DisplayName("AVAILABLE 상태인 StoredBook 중 첫 번째를 반환한다")
    @Test
    void returnsFirstAvailableBook() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        storedBooks.add(StoredBook.createOnHold(dummyBook()));
        storedBooks.add(StoredBook.createAvailable(dummyBook()));
        // when
        Optional<StoredBook> result = storedBooks.firstAvailable();
        // then
        assertThat(result.get().getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }

    private static void setId(StoredBook target, long id) {
        try {
            Field field = StoredBook.class.getDeclaredField("id");
            field.setAccessible(true);
            field.setLong(target, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("AVAILABLE 상태가 없으면 Optional.empty()를 반환한다")
    @Test
    void returnsEmptyWhenNoAvailableBook() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        storedBooks.add(StoredBook.createOnHold(dummyBook()));
        // when
        Optional<StoredBook> result = storedBooks.firstAvailable();
        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("일치하는 id가 있으면 해당 StoredBook을 반환한다")
    @Test
    void returnsMatchingStoredBook() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook sb1 = StoredBook.createAvailable(dummyBook());
        StoredBook sb2 = StoredBook.createOnHold(dummyBook());
        storedBooks.add(sb1);
        storedBooks.add(sb2);
        setId(sb1, 101L);
        setId(sb2, 202L);
        // when
        Optional<StoredBook> found1 = storedBooks.findById(101L);
        Optional<StoredBook> found2 = storedBooks.findById(202L);
        // then
        assertThat(found1).isPresent().contains(sb1);
        assertThat(found2).isPresent().contains(sb2);
    }

    @DisplayName("일치하는 id가 없으면 Optional.empty()를 반환한다")
    @Test
    void returnsEmptyWhenNotFound() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook sb = StoredBook.createAvailable(dummyBook());
        storedBooks.add(sb);
        setId(sb, 101L);
        // when
        Optional<StoredBook> notFound = storedBooks.findById(999L);
        // then
        assertThat(notFound).isEmpty();
    }

    @DisplayName("전달한 id들의 소장본을 AVAILABLE로 복귀시킨다")
    @Test
    void changesStatusToAvailable() {
        // given
        StoredBooks storedBooks = new StoredBooks();
        StoredBook hold = StoredBook.createOnHold(dummyBook());
        StoredBook loaned = StoredBook.createAvailable(dummyBook());
        loaned.loan();
        storedBooks.add(hold);
        storedBooks.add(loaned);
        setId(hold, 11L);
        setId(loaned, 22L);
        // when
        storedBooks.restoreAllToAvailable(List.of(11L, 22L));
        // then
        assertThat(hold.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
        assertThat(loaned.getStatus()).isEqualTo(StoredBookStatus.AVAILABLE);
    }
    private Book dummyBook() {
        return Book.registerNew("테스트 제목", "테스트 저자", 1);
    }
}
