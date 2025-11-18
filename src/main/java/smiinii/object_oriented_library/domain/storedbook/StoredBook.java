package smiinii.object_oriented_library.domain.storedbook;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.Book;

@Entity
public class StoredBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private StoredBookStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    protected StoredBook() {}

    private StoredBook(Book book, StoredBookStatus status) {
        this.book = book;
        this.status = status;
    }

    public static StoredBook createAvailable(Book book) {
        return new StoredBook(book, StoredBookStatus.AVAILABLE);
    }

    public static StoredBook createOnHold(Book book) {
        return new StoredBook(book, StoredBookStatus.ON_HOLD);
    }

    public void loan() {
        if (status.isLoaned()) {
            throw new IllegalStateException("이미 대출 중입니다.");
        }
        if (status.isOnHold()) {
            throw new IllegalStateException("다른 회원이 예약 중입니다.");
        }
        this.status = StoredBookStatus.LOANED;
    }

    public void returnBook(boolean isReservation) {
        if (!status.isLoaned()) {
            throw new IllegalStateException("대출 중인 도서만 반납할 수 있습니다.");
        }
        if (isReservation) {
            this.status = StoredBookStatus.ON_HOLD;
            return;
        }
        this.status = StoredBookStatus.AVAILABLE;
    }

    public void toOnHold() {
        if (!status.isAvailable()) {
            throw new IllegalStateException("예약은 AVAILABLE 상태에서만 가능합니다.");
        }
        this.status = StoredBookStatus.ON_HOLD;
    }

    public void returnToAvailable() {
        this.status = StoredBookStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status.isAvailable();
    }

    public boolean hasId(Long targetId) {
        return this.id == targetId;
    }

    public StoredBookStatus getStatus() {
        return status;
    }

    public Long getId() {
        return id;
    }

    public Book getBook() {
        return book;
    }
}
