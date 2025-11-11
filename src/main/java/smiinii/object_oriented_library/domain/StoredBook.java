package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;

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

    private StoredBook(Book bookId, StoredBookStatus status) {
        this.book = bookId;
        this.status = status;
    }

    public static StoredBook createAvailable(Book bookId) {
        return new StoredBook(bookId, StoredBookStatus.AVAILABLE);
    }

    public static StoredBook createOnHold(Book bookId) {
        return new StoredBook(bookId, StoredBookStatus.ON_HOLD);
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
}
