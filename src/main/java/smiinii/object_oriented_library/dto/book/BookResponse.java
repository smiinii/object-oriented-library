package smiinii.object_oriented_library.dto.book;

import smiinii.object_oriented_library.domain.Book;

public class BookResponse {

    private final Long id;
    private final String title;
    private final String author;
    private final int copyCount;

    public BookResponse(Long id, String title, String author, int copyCount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.copyCount = copyCount;
    }

    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getStoredBooks().size()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getCopyCount() {
        return copyCount;
    }
}
