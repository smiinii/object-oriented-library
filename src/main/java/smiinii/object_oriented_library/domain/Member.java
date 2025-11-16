package smiinii.object_oriented_library.domain;

import jakarta.persistence.*;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MemberStatus memberStatus;

    @Embedded
    private Penalty penalty;

    @Column(nullable = false)
    private String name;

    protected Member() {}

    private Member(String name) {
        this.name = name;
        this.memberStatus = MemberStatus.ACTIVE;
        this.penalty = null;
    }

    public static Member create(String name) {
        return new Member(name);
    }

    public boolean canBorrow(Clock clock) {
        return memberStatus == MemberStatus.ACTIVE;
    }

    public void applyPenalty(Penalty penalty) {
        this.penalty = penalty;
        this.memberStatus = MemberStatus.SUSPENDED;
    }

    public void releasePenaltyIfExpired(Clock clock) {
        if (penalty == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(clock);

        if (!penalty.isActive(now)) {
            this.penalty = null;
            this.memberStatus = MemberStatus.ACTIVE;
        }
    }

    public boolean hasPenalty() {
        return penalty != null;
    }

    public String getPenaltyReason() {
        if (penalty == null) {
            throw new IllegalStateException("현재 패널티가 존재하지 않습니다.");
        }
        return penalty.getReason();
    }

    public LocalDateTime getPenaltyStartsAt() {
        if (penalty == null) {
            throw new IllegalStateException("현재 패널티가 존재하지 않습니다.");
        }
        return penalty.getStartsAt();
    }

    public LocalDateTime getPenaltyEndsAt() {
        if (penalty == null) {
            throw new IllegalStateException("현재 패널티가 존재하지 않습니다.");
        }
        return penalty.getEndsAt();
    }

    public MemberStatus getMemberStatus() {
        return memberStatus;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
