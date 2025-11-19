package smiinii.object_oriented_library.dto.book;

public class AddBooksRequest {

    private int count;

    public AddBooksRequest(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
