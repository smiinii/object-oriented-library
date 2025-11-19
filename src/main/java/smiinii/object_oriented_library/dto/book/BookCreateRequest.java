package smiinii.object_oriented_library.dto.book;

public class BookCreateRequest {

    private String title;
    private String author;
    private int initialCount;

    public BookCreateRequest(String title, String author, int initialCount) {
        this.title = title;
        this.author = author;
        this.initialCount = initialCount;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getInitialCount() {
        return initialCount;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setInitialCount(int initialCount) {
        this.initialCount = initialCount;
    }
}
