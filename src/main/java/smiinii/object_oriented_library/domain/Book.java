package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.reservation.Reservations;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;
    private String author;

    @Embedded
    private StoredBooks storedBooks = new StoredBooks();

    @Embedded
    private Reservations reservations = new Reservations();

    protected Book() {}

    private Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public static Book registerNew(String title, String author, int initialCount) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목 필요");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("저자 필요");
        }
        if (initialCount < 0) {
            throw new IllegalArgumentException("초기 소장본 수는 0 이상");
        }
        Book book = new Book(title, author);
        if (initialCount > 0) {
            book.addAvailableStoredBooks(initialCount);
        }
        return book;
    }

    public void addAvailableStoredBooks(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("추가 수량은 1 이상");
        }
        for (int i = 0; i < count; i++) {
            StoredBook sb = StoredBook.createAvailable();
            registerStoredBook(sb);
        }
    }

    public void registerStoredBook(StoredBook storedBook) {
        if (storedBook == null) {
            throw new IllegalArgumentException("추가할 소장본이 비어있습니다.");
        }
        storedBook.setBook(this);
        storedBooks.add(storedBook);
    }
}
