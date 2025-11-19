package smiinii.object_oriented_library.dto.loan;

public class LoanCreateRequest {

    private Long memberId;
    private Long bookId;

    public LoanCreateRequest(Long memberId, Long bookId) {
        this.memberId = memberId;
        this.bookId = bookId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
