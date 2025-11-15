package smiinii.object_oriented_library.domain.loan_policy;

import java.time.LocalDateTime;

public interface LoanPolicy {
    LocalDateTime initialDueDate(LocalDateTime loanedAt);
    LocalDateTime extendedDueDate(LocalDateTime dueDate);
}
