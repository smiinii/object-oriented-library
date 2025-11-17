package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.reservation.Reservations;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;
import smiinii.object_oriented_library.domain.storedbook.StoredBooks;

import java.time.Clock;
import java.time.Duration;
import java.util.Optional;

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
            throw new IllegalArgumentException("제목이 필요합니다.");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("저자가 필요합니다.");
        }
        if (initialCount <= 0) {
            throw new IllegalArgumentException("초기 소장본 수는 1개 이상이어야 합니다.");
        }
        Book book = new Book(title, author);
        book.addAvailableStoredBooks(initialCount);
        return book;
    }

    public void addAvailableStoredBooks(int count) {
        for (int i = 0; i < count; i++) {
            StoredBook storedBook = StoredBook.createAvailable(this);
            storedBooks.add(storedBook);
        }
    }

    public boolean tryReserve(Long memberId, int maxQueueSize, Clock clock) {
        if (!storedBooks.allLoaned()) {
            return false;
        }
        return reservations.tryReserve(this, memberId, maxQueueSize, clock);
    }

    public void assignHoldIfReservationExists(Duration holdDuration, Clock clock) {
        Optional<StoredBook> firstStoredBook = storedBooks.firstAvailable();
        if (firstStoredBook.isEmpty()) {
            return;
        }
        StoredBook storedBook = firstStoredBook.get();

        boolean assigned = reservations
                .assignHoldToHeadIfExists(storedBook.getId(), holdDuration, clock)
                .isPresent();

        if (assigned) {
            storedBook.toOnHold();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public StoredBooks getStoredBooks() {
        return storedBooks;
    }

    public Long getId() {
        return id;
    }
}
