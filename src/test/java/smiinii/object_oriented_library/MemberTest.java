package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.domain.MemberStatus;
import smiinii.object_oriented_library.domain.Penalty;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberTest {

    private Clock fixedClock(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("create: 신규 회원을 생성하면 ACTIVE 상태이고 패널티가 없다")
    void createsActiveMemberWithoutPenalty() {
        // given
        String name = "이성민";
        // when
        Member member = Member.create(name);
        // then
        assertThat(member).isNotNull();
        assertThat(member.getName()).isEqualTo(name);
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.hasPenalty()).isFalse();
    }

    @Test
    @DisplayName("canBorrow: ACTIVE이고 패널티가 없으면 대출이 가능하다")
    void whenActiveAndNoPenaltyReturnsTrue() {
        // given
        Member member = Member.create("이성민");
        // when
        boolean result = member.canBorrow();
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canBorrow: 회원 상태가 SUSPENDED이면 대출이 불가능하다")
    void whenSuspendedReturnsFalse() {
        // given
        Member member = Member.create("이성민");

        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 16, 9, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 17, 9, 0);
        Penalty penalty = Penalty.of(startsAt, endsAt, "연체 1일");
        member.applyPenalty(penalty);

        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.SUSPENDED);
        // when
        boolean result = member.canBorrow();
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("applyPenalty: 패널티를 적용하면 패널티가 설정되고 상태가 SUSPENDED로 변경된다")
    void applyPenaltySetsPenaltyAndSuspendsMember() {
        // given
        Member member = Member.create("이성민");
        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 16, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 18, 10, 0);
        Penalty penalty = Penalty.of(startsAt, endsAt, "연체 2일");
        // when
        member.applyPenalty(penalty);
        // then
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.SUSPENDED);
        assertThat(member.hasPenalty()).isTrue();
        assertThat(member.getPenaltyReason()).isEqualTo("연체 2일");
        assertThat(member.getPenaltyStartsAt()).isEqualTo(startsAt);
        assertThat(member.getPenaltyEndsAt()).isEqualTo(endsAt);
    }

    @Test
    @DisplayName("releasePenaltyIfExpired: 패널티 기간이 지났다면 패널티를 해제하고 상태를 ACTIVE로 되돌린다")
    void releasePenaltyIfExpiredWhenExpiredClearsPenaltyAndActivate() {
        // given
        Member member = Member.create("이성민");
        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 10, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 11, 10, 0);
        Penalty penalty = Penalty.of(startsAt, endsAt, "연체 1일");
        member.applyPenalty(penalty);

        Clock afterPenaltyClock = fixedClock(LocalDateTime.of(2025, 11, 16, 10, 0));
        // when
        member.releasePenaltyIfExpired(afterPenaltyClock);
        // then
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.hasPenalty()).isFalse();
    }

    @Test
    @DisplayName("releasePenaltyIfExpired: 패널티가 없으면 아무 변화도 일어나지 않는다")
    void releasePenaltyWithoutPenaltyDoesNothing() {
        // given
        Member member = Member.create("이성민");
        Clock clock = fixedClock(LocalDateTime.of(2025, 11, 16, 10, 0));
        // when
        member.releasePenaltyIfExpired(clock);
        // then
        assertThat(member.getMemberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.hasPenalty()).isFalse();
    }
}
