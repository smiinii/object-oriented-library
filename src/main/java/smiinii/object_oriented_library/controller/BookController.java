package smiinii.object_oriented_library.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.dto.book.AddBooksRequest;
import smiinii.object_oriented_library.dto.book.BookCreateRequest;
import smiinii.object_oriented_library.dto.book.BookResponse;
import smiinii.object_oriented_library.service.BookService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<Long> registerBook(@RequestBody BookCreateRequest request) {
        Long bookId = bookService.registerBook(
                request.getTitle(),
                request.getAuthor(),
                request.getInitialCount()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(bookId);
    }

    @PostMapping("/{bookId}/copies")
    public ResponseEntity<Void> addStoredBooks(@PathVariable Long bookId,
                                               @RequestBody AddBooksRequest request) {
        bookService.addStoredBooks(bookId, request.getCount());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long bookId) {
        Book book = bookService.getBook(bookId);
        return ResponseEntity.ok(BookResponse.from(book));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getBooks() {
        List<Book> books = bookService.getBooks();
        List<BookResponse> responses = books.stream()
                .map(BookResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
