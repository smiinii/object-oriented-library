package smiinii.object_oriented_library.domain;

public enum StoredBookStatus {
    AVAILABLE,
    LOANED,
    ON_HOLD;

    public boolean isAvailable() {
        return this == AVAILABLE;
    }

    public boolean isOnHold() {
        return this == ON_HOLD;
    }

    public boolean isLoaned() {
        return this == LOANED;
    }
}
