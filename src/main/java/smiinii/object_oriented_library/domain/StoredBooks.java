package smiinii.object_oriented_library.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Embeddable
public class StoredBooks {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoredBook> storedBooks = new ArrayList<>();

    public void add (StoredBook storedBook) {
        if (storedBook == null) {
            throw new IllegalArgumentException("소장본에 추가할 책이 비어있습니다.");
        }
        storedBooks.add(storedBook);
    }

    public boolean allLoaned() {
        if (storedBooks.isEmpty()) {
            throw new IllegalArgumentException("소장본이 비어있습니다.");
        }
        return storedBooks.stream()
                .allMatch(storedBook ->
                        storedBook.getStatus() == StoredBookStatus.LOANED);
    }

    public List<StoredBook> getStoredBooks() {
        return List.copyOf(storedBooks);
    }
}
