package smiinii.object_oriented_library.domain.reservation_policy;

import smiinii.object_oriented_library.domain.StoredBook;
import smiinii.object_oriented_library.domain.StoredBooks;

import java.time.Duration;
import java.util.Optional;

public interface ReservationPolicy {
    int maxQueueSize();
    Duration holdDuration();
    Optional<StoredBook> selectStoredBook(StoredBooks storedBooks);
}
