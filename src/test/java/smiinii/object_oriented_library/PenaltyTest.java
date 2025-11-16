package smiinii.object_oriented_library;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import smiinii.object_oriented_library.domain.Penalty;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PenaltyTest {

    @Test
    @DisplayName("of: 시작 시각, 종료 시각, 사유로 Penalty를 생성할 수 있다")
    void createPenalty() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 10, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 12, 10, 0);
        String reason = "연체 1일";
        // when
        Penalty penalty = Penalty.of(startsAt, endsAt, reason);
        // then
        assertThat(penalty.getStartsAt()).isEqualTo(startsAt);
        assertThat(penalty.getEndsAt()).isEqualTo(endsAt);
        assertThat(penalty.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("isActive: now가 startsAt 이상이고 endsAt 미만이면 활성 상태이다")
    void isActiveDuringPeriod() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 10, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 12, 10, 0);
        Penalty penalty = Penalty.of(startsAt, endsAt, "연체");

        LocalDateTime now = LocalDateTime.of(2025, 11, 11, 10, 0);
        // when
        boolean active = penalty.isActive(now);
        // then
        assertThat(active).isTrue();
    }

    @Test
    @DisplayName("isActive: now가 기간 밖이면 비활성 상태이다")
    void isInactiveOutsidePeriod() {
        // given
        LocalDateTime startsAt = LocalDateTime.of(2025, 11, 10, 10, 0);
        LocalDateTime endsAt = LocalDateTime.of(2025, 11, 12, 10, 0);
        Penalty penalty = Penalty.of(startsAt, endsAt, "연체");
        // when
        boolean before = penalty.isActive(LocalDateTime.of(2025, 11, 10, 9, 59));
        boolean after = penalty.isActive(LocalDateTime.of(2025, 11, 12, 10, 1));
        // then
        assertThat(before).isFalse();
        assertThat(after).isFalse();
    }
}
