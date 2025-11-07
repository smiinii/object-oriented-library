package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;

@Entity
public class StoredBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private StoredBookStatus status;

    private long bookId;

    private StoredBook(long bookId, StoredBookStatus status) {
        this.bookId = bookId;
        this.status = status;
    }

    public static StoredBook createAvailable(long bookId) {
        return new StoredBook(bookId, StoredBookStatus.AVAILABLE);
    }

    public static StoredBook createOnHold(long bookId) {
        return new StoredBook(bookId, StoredBookStatus.ON_HOLD);
    }

    public void loan() {
        if (status == StoredBookStatus.LOANED) {
            throw new IllegalStateException("이미 대출 중입니다.");
        }
        if (status == StoredBookStatus.ON_HOLD) {
            throw new IllegalStateException("다른 회원이 예약 중입니다.");
        }
        this.status = StoredBookStatus.LOANED;
    }

    public void returnBook(boolean isReservation) {
        if (status != StoredBookStatus.LOANED) {
            throw new IllegalStateException("대출 중인 도서만 반납할 수 있습니다.");
        }
        if (isReservation) {
            this.status = StoredBookStatus.ON_HOLD;
            return;
        }
        this.status = StoredBookStatus.AVAILABLE;
    }

    public StoredBookStatus getStatus() {
        return status;
    }
}
