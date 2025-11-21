package smiinii.object_oriented_library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smiinii.object_oriented_library.dto.loan.LoanCreateRequest;
import smiinii.object_oriented_library.dto.loan.LoanCreateResponse;
import smiinii.object_oriented_library.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanCreateResponse> createLoan(@RequestBody LoanCreateRequest request) {
        Long loanId = loanService.loan(request.getMemberId(), request.getBookId());
        return ResponseEntity.ok(new LoanCreateResponse(loanId));
    }

    @PostMapping("/{loanId}/return")
    public ResponseEntity<Void> returnBook(@PathVariable Long loanId) {
        loanService.returnBook(loanId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{loanId}/extend")
    public ResponseEntity<Void> extendLoan(@PathVariable Long loanId) {
        loanService.extend(loanId);
        return ResponseEntity.noContent().build();
    }
}
