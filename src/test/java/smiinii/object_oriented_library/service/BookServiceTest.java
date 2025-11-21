package smiinii.object_oriented_library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.dto.book.BookResponse;
import smiinii.object_oriented_library.repository.BookRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    @Test
    @DisplayName("registerBook: 새 도서를 등록하면 Book.registerNew로 생성된 엔티티를 저장하고 생성된 ID를 반환한다")
    void savesBookAndReturnsId() throws Exception {
        // given
        String title = "클린 코드";
        String author = "로버트 마틴";
        int initialCount = 3;

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(book, 1L);
            return book;
        });
        // when
        Long savedId = bookService.registerBook(title, author, initialCount);
        // then
        assertThat(savedId).isEqualTo(1L);

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository, times(1)).save(captor.capture());

        Book savedBookArg = captor.getValue();
        assertThat(savedBookArg.getTitle()).isEqualTo(title);
        assertThat(savedBookArg.getAuthor()).isEqualTo(author);
        assertThat(savedBookArg.getStoredBooks().size()).isEqualTo(initialCount);
    }

    @Test
    @DisplayName("addStoredBooks: 기존 도서에 소장본을 추가하면 개수가 증가한다")
    void increasesCopyCount() {
        // given
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 1);
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // when
        bookService.addStoredBooks(bookId, 2);
        // then
        assertThat(book.getStoredBooks().size()).isEqualTo(3);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("addStoredBooks: 존재하지 않는 도서 ID이면 예외를 던진다")
    void whenBookNotFoundThrowsException() {
        // given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> bookService.addStoredBooks(bookId, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 도서입니다.");
    }

    @Test
    @DisplayName("getBook: 도서 ID로 단건 조회할 수 있다")
    void returnsBook() {
        // given
        Long bookId = 1L;
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 1);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // when
        BookResponse result = bookService.getBook(bookId);
        // then
        assertThat(result.getTitle()).isEqualTo("클린 코드");
        assertThat(result.getAuthor()).isEqualTo("로버트 마틴");
        assertThat(result.getCopyCount()).isEqualTo(1);

        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("getBook: 존재하지 않는 도서 ID면 예외를 던진다")
    void whenNotFoundThrowsException() {
        // given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> bookService.getBook(bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 도서입니다.");
    }

    @Test
    @DisplayName("getBooks: 전체 도서 목록을 조회할 수 있다")
    void returnsAllBooks() {
        // given
        Book book1 = Book.registerNew("클린 코드", "로버트 마틴", 1);
        Book book2 = Book.registerNew("리팩터링", "마틴 파울러", 2);
        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));
        // when
        List<BookResponse> result = bookService.getBooks();
        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(BookResponse::getTitle)
                .containsExactly("클린 코드", "리팩터링");
        assertThat(result)
                .extracting(BookResponse::getAuthor)
                .containsExactly("로버트 마틴", "마틴 파울러");

        verify(bookRepository, times(1)).findAll();
    }
}
