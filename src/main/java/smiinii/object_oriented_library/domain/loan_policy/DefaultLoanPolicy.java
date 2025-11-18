package smiinii.object_oriented_library.domain.loan_policy;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DefaultLoanPolicy implements LoanPolicy {

    @Override
    public LocalDateTime initialDueDate(LocalDateTime loanedAt) {
        return loanedAt.plusDays(7);
    }

    @Override
    public LocalDateTime extendedDueDate(LocalDateTime dueDate) {
        return dueDate.plusDays(7);
    }
}
