package smiinii.object_oriented_library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;

public interface StoredBookRepository extends JpaRepository<StoredBook, Long> {
}
