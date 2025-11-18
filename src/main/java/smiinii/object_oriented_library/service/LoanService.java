package smiinii.object_oriented_library.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.Loan;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.domain.Penalty;
import smiinii.object_oriented_library.domain.loan_policy.LoanPolicy;
import smiinii.object_oriented_library.domain.reservation_policy.ReservationPolicy;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;
import smiinii.object_oriented_library.repository.BookRepository;
import smiinii.object_oriented_library.repository.LoanRepository;
import smiinii.object_oriented_library.repository.MemberRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class LoanService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationPolicy reservationPolicy;
    private final LoanPolicy loanPolicy;
    private final Clock clock;

    public LoanService(
            MemberRepository memberRepository,
            BookRepository bookRepository,
            LoanRepository loanRepository,
            ReservationPolicy reservationPolicy,
            LoanPolicy loanPolicy,
            Clock clock
    ) {
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.reservationPolicy = reservationPolicy;
        this.loanPolicy = loanPolicy;
        this.clock = clock;
    }

    @Transactional
    public Long loan(Long memberId, Long bookId) {
        LocalDateTime now = LocalDateTime.now(clock);

        Member member = validateMember(memberId);
        Book book = checkBook(bookId);
        StoredBook storedBook = checkStoredBook(book);

        LocalDateTime dueDate = loanPolicy.initialDueDate(now);
        Loan loan = Loan.of(member, storedBook, now, dueDate);

        storedBook.loan();
        loanRepository.save(loan);

        return loan.getId();
    }

    @Transactional
    public void returnBook(Long loanId) {
        Loan loan = checkLoan(loanId);
        if (!loan.isActive()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        Member member = loan.getMember();
        StoredBook storedBook = loan.getStoredBook();
        Book book = storedBook.getBook();

        loan.returnBook(now, false);
        book.assignHoldIfReservationExists(reservationPolicy.holdDuration(), clock);
        applyPenaltyIfOverdue(loan, member, now);
    }

    @Transactional
    public void extend(Long loanId) {
        Loan loan = checkLoan(loanId);
        if (!loan.isActive()) {
            throw new IllegalStateException("반납된 대출은 연장할 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now(clock);

        Member member = loan.getMember();
        StoredBook storedBook = loan.getStoredBook();
        Book book = storedBook.getBook();

        boolean hasReservation = book.hasActiveReservationByOtherMember(member.getId());
        LocalDateTime newDueDate = loanPolicy.extendedDueDate(loan.getDueDate());
        loan.extend(now, hasReservation, newDueDate);
    }

    private Member validateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        member.releasePenaltyIfExpired(clock);

        if (!member.canBorrow()) {
            throw new IllegalStateException("현재 대출이 불가능한 회원입니다.");
        }
        return member;
    }

    private Book checkBook(Long bookId) {
         return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));
    }

    private StoredBook checkStoredBook(Book book) {
        return book.getStoredBooks().firstAvailable()
                .orElseThrow(() -> new IllegalStateException("대출 가능한 소장본이 없습니다."));
    }

    private Loan checkLoan(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 대출입니다."));
    }

    private void applyPenaltyIfOverdue(Loan loan, Member member, LocalDateTime now) {
        if (loan.isOverdue(now)) {
            long overdueDays = Math.max(1, Duration.between(loan.getDueDate(), now).toDays());
            LocalDateTime startsAt = now;
            LocalDateTime endsAt = now.plusDays(overdueDays);
            String reason = "연체 " + overdueDays + "일";

            Penalty penalty = Penalty.of(startsAt, endsAt, reason);
            member.applyPenalty(penalty);
        }
    }
}
