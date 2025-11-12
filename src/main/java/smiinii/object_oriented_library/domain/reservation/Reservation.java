package smiinii.object_oriented_library.domain.reservation;

import jakarta.persistence.*;
import smiinii.object_oriented_library.domain.Book;

import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    private Long memberId; // 예약한 사람
    private Long holdStoredBookId; // 예약한 책
    private LocalDateTime createdAt; // 예약 시간
    private LocalDateTime holdUntil; // 예약 만료 시간

    protected Reservation() {}

    private Reservation(Book book, Long memberId, LocalDateTime createdAt) {
        this.book = book;
        this.memberId = memberId;
        this.reservationStatus = ReservationStatus.QUEUED;
        this.createdAt = createdAt;
    }

    public static Reservation create(Book book, Long memberId, LocalDateTime createdAt) {
        return new Reservation(book, memberId, createdAt);
    }

    public void prepareHold(Long storedBookId, LocalDateTime holdUntil) {
        if (!reservationStatus.isQueued()) {
            throw new IllegalArgumentException("예약 대기열에 존재하지 않습니다.");
        }
        this.reservationStatus = ReservationStatus.HOLD_READY;
        this.holdStoredBookId = storedBookId;
        this.holdUntil = holdUntil;
    }

    public void expire(LocalDateTime now) {
        if (reservationStatus.isHoldReady() && holdUntil != null && now.isAfter(holdUntil)) {
            this.reservationStatus = ReservationStatus.EXPIRED;
            this.holdStoredBookId = null;
            this.holdUntil = null;
        }
    }

    public void complete(LocalDateTime now) {
        if (!reservationStatus.isHoldReady()) {
            throw new IllegalArgumentException("대출 대기 상태가 아닙니다.");
        }
        if (holdUntil != null && now.isAfter(holdUntil)) {
            throw new IllegalArgumentException("대출 대기 기한이 만료되었습니다.");
        }
        this.reservationStatus = ReservationStatus.LOAN_COMPLETE;
    }

    public boolean matchesHold(Long storedBookId) {
        return isHoldReady() && storedBookId != null &&
                storedBookId.equals(this.holdStoredBookId);
    }

    public boolean sameMember(Long memberId) {
        return memberId != null && memberId.equals(this.memberId);
    }

    public boolean isActive() {
        return reservationStatus.isActive();
    }

    public boolean isQueued() {
        return reservationStatus.isQueued();
    }

    public boolean isHoldReady() {
        return reservationStatus.isHoldReady();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public Long getHoldStoredBookId() {
        return holdStoredBookId;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public Long getMemberId() {
        return memberId;
    }
}
