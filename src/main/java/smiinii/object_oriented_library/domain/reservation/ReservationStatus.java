package smiinii.object_oriented_library.domain.reservation;

public enum ReservationStatus {
    QUEUED, // 대기열
    HOLD_READY, // 우선권 부여
    LOAN_COMPLETE, // 대출 완료
    EXPIRED; // 만료

    public boolean isActive() {
        return this == QUEUED || this == HOLD_READY;
    }

    public boolean isQueued() {
        return this == QUEUED;
    }

    public boolean isHoldReady() {
        return this == HOLD_READY;
    }
}
