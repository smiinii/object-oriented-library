package smiinii.object_oriented_library.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StoredBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private final long bookId;
    private StoredBookStatus status;

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

    public void loan(Long memberId) {
        if (status == StoredBookStatus.LOANED) {
            throw new IllegalStateException("이미 대출 중입니다.");
        }
        if (status == StoredBookStatus.ON_HOLD) {
            throw new IllegalStateException("다른 회원이 예약 중입니다.");
        }
        this.status = StoredBookStatus.LOANED;
    }

    public StoredBookStatus getStatus() {
        return status;
    }
}
