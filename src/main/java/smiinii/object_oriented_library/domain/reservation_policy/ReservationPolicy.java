package smiinii.object_oriented_library.domain.reservation_policy;

import java.time.Duration;

public interface ReservationPolicy {
    int maxQueueSize();
    Duration holdDuration();
}
