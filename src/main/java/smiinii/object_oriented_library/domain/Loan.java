package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;

import java.time.LocalDateTime;

@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stored_book_id", nullable = false)
    private StoredBook storedBook;

    private LocalDateTime loanedAt; // 대출 시간
    private LocalDateTime dueDate; // 반납 예정일
    private LocalDateTime returnedAt; // 반납 시간

    protected Loan() {}

    private Loan(Member member, StoredBook storedBook, LocalDateTime loanedAt, LocalDateTime dueDate) {
        this.member = member;
        this.storedBook = storedBook;
        this.loanedAt = loanedAt;
        this.dueDate = dueDate;
    }

    public static Loan of(Member member, StoredBook storedBook, LocalDateTime loanedAt, LocalDateTime dueDate) {
        return new Loan(member, storedBook, loanedAt, dueDate);
    }

    public void returnBook(LocalDateTime now, boolean hasReservation) {
       if (!isActive()) {
           throw new IllegalArgumentException("대출 중인 도서만 반납할 수 있습니다.");
       }
       this.returnedAt = now;
       storedBook.returnBook(hasReservation);
    }

    public void extend(LocalDateTime now, boolean hasReservation, LocalDateTime newDueDate) {
        if (!isActive()) {
            throw new IllegalStateException("대출 중인 도서만 연장할 수 있습니다.");
        }
        if (isOverdue(now)) {
            throw new IllegalStateException("연체 중인 도서는 연장할 수 없습니다.");
        }
        if (hasReservation) {
            throw new IllegalStateException("다른 회원의 예약 대기열이 존재해 연장할 수 없습니다.");
        }
        this.dueDate = newDueDate;
    }

    public boolean isActive() {
        return returnedAt == null;
    }

    public boolean isOverdue(LocalDateTime now) {
        return isActive() && now.isAfter(dueDate);
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public Member getMember() {
        return member;
    }

    public StoredBook getStoredBook() {
        return storedBook;
    }

    public LocalDateTime getLoanedAt() {
        return loanedAt;
    }
}
