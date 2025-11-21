package smiinii.object_oriented_library.domain.reservation_policy;

import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class DefaultReservationPolicy implements ReservationPolicy {

    @Override
    public int maxQueueSize() {
        return 3;
    }

    @Override
    public Duration holdDuration() {
        return Duration.ofDays(3);
    }
}
