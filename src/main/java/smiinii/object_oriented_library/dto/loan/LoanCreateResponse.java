package smiinii.object_oriented_library.dto.loan;

public class LoanCreateResponse {

    private final Long loanId;

    public LoanCreateResponse(Long loanId) {
        this.loanId = loanId;
    }

    public Long getLoanId() {
        return loanId;
    }
}
