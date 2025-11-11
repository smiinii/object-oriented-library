package smiinii.object_oriented_library.domain.reservation_policy;

import org.springframework.stereotype.Component;
import smiinii.object_oriented_library.domain.StoredBook;
import smiinii.object_oriented_library.domain.StoredBooks;

import java.time.Duration;
import java.util.Optional;

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

    @Override
    public Optional<StoredBook> selectStoredBook(StoredBooks storedBooks) {
        return storedBooks.firstAvailable();
    }
}
