package smiinii.object_oriented_library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.repository.BookRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public Long registerBook(String title, String author, int initialCount) {
        Book book = Book.registerNew(title, author, initialCount);
        Book saved = bookRepository.save(book);
        return saved.getId();
    }

    @Transactional
    public void addStoredBooks(Long bookId, int count) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));
        book.addAvailableStoredBooks(count);
    }

    public Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }
}
