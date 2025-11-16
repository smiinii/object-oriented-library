package smiinii.object_oriented_library.domain;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class Penalty {

    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String reason;

    protected Penalty() {}

    private Penalty(LocalDateTime startsAt, LocalDateTime endsAt, String reason) {
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.reason = reason;
    }

    public static Penalty of(LocalDateTime startsAt, LocalDateTime endsAt, String reason) {
        return new Penalty(startsAt, endsAt, reason);
    }

    public boolean isActive(LocalDateTime now) {
        return !now.isBefore(startsAt) && now.isBefore(endsAt);
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }
}
