package smiinii.object_oriented_library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smiinii.object_oriented_library.domain.*;
import smiinii.object_oriented_library.domain.loan_policy.LoanPolicy;
import smiinii.object_oriented_library.domain.reservation_policy.ReservationPolicy;
import smiinii.object_oriented_library.domain.storedbook.StoredBook;
import smiinii.object_oriented_library.repository.BookRepository;
import smiinii.object_oriented_library.repository.LoanRepository;
import smiinii.object_oriented_library.repository.MemberRepository;

import java.lang.reflect.Field;
import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReservationPolicy reservationPolicy;

    @Mock
    private LoanPolicy loanPolicy;

    @Mock
    private Clock clock;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(memberRepository, bookRepository,
                loanRepository, reservationPolicy, loanPolicy, clock);
    }

    private void mockClock(LocalDateTime now) {
        ZoneId zone = ZoneId.of("UTC");
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(now.toInstant(ZoneOffset.UTC));
    }

    @Test
    @DisplayName("loan: 회원과 도서가 존재하고 가용 소장본이 있으면 대출을 생성하고 ID를 반환한다")
    void loanCreatesAndSavesLoan() throws Exception {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        LocalDateTime now = LocalDateTime.of(2025, 11, 18, 10, 0);
        mockClock(now);

        Member member = Member.create("이성민");
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 1);
        StoredBook availableCopy = book.getStoredBooks().firstAvailable().orElseThrow();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        LocalDateTime dueDate = now.plusDays(7);
        when(loanPolicy.initialDueDate(now)).thenReturn(dueDate);

        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            Field idField = Loan.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(loan, 100L);
            return loan;
        });
        // when
        Long loanId = loanService.loan(memberId, bookId);
        // then
        assertThat(loanId).isEqualTo(100L);

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository, times(1)).save(captor.capture());

        Loan savedLoan = captor.getValue();
        assertThat(savedLoan.getMember()).isSameAs(member);
        assertThat(savedLoan.getStoredBook()).isSameAs(availableCopy);
        assertThat(savedLoan.getLoanedAt()).isEqualTo(now);
        assertThat(savedLoan.getDueDate()).isEqualTo(dueDate);
    }

    @Test
    @DisplayName("loan: 존재하지 않는 회원이면 예외를 던진다")
    void loanThrowsWhenMemberNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> loanService.loan(memberId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("loan: 존재하지 않는 도서이면 예외를 던진다")
    void loanThrowsWhenBookNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Member member = Member.create("이성민");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> loanService.loan(memberId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 도서입니다.");
    }

    @Test
    @DisplayName("loan: 대출 가능한 소장본이 없으면 예외를 던진다")
    void loanThrowsWhenNoAvailableCopy() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        LocalDateTime now = LocalDateTime.of(2025, 11, 18, 10, 0);
        mockClock(now);

        Member member = Member.create("이성민");
        Book book = Book.registerNew("클린 코드", "로버트 마틴", 1);
        // 유일한 소장본을 대출 상태로 바꿔서 AVAILABLE 없음 만들기
        StoredBook storedBook = book.getStoredBooks().firstAvailable().orElseThrow();
        storedBook.loan();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // when & then
        assertThatThrownBy(() -> loanService.loan(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대출 가능한 소장본이 없습니다.");
    }

    @Test
    @DisplayName("returnBook: 이미 반납된 대출이면 아무 것도 하지 않는다")
    void returnBookDoesNothingWhenAlreadyReturned() {
        // given
        Long loanId = 1L;
        Loan loan = mock(Loan.class);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loan.isActive()).thenReturn(false);
        // when
        loanService.returnBook(loanId);
        // then
        verify(loan, times(1)).isActive();
        verifyNoMoreInteractions(loan);
    }

    @Test
    @DisplayName("returnBook: 연체가 아닌 경우 예약 처리만 하고 패널티는 부여하지 않는다")
    void returnBookWithoutOverdue() {
        // given
        Long loanId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 11, 18, 10, 0);
        mockClock(now);

        Loan loan = mock(Loan.class);
        Member member = mock(Member.class);
        StoredBook storedBook = mock(StoredBook.class);
        Book book = mock(Book.class);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loan.isActive()).thenReturn(true);
        when(loan.getMember()).thenReturn(member);
        when(loan.getStoredBook()).thenReturn(storedBook);
        when(storedBook.getBook()).thenReturn(book);

        Duration holdDuration = Duration.ofDays(3);
        when(reservationPolicy.holdDuration()).thenReturn(holdDuration);
        when(loan.isOverdue(now)).thenReturn(false);
        // when
        loanService.returnBook(loanId);
        // then
        verify(loan).returnBook(now, false);
        verify(book).assignHoldIfReservationExists(holdDuration, clock);
        verify(member, never()).applyPenalty(any());
    }

    @Test
    @DisplayName("returnBook: 연체된 대출 반납 시 패널티를 부여한다")
    void returnBookWithOverdueAppliesPenalty() {
        // given
        Long loanId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 11, 18, 10, 0);
        mockClock(now);

        Loan loan = mock(Loan.class);
        Member member = mock(Member.class);
        StoredBook storedBook = mock(StoredBook.class);
        Book book = mock(Book.class);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loan.isActive()).thenReturn(true);
        when(loan.getMember()).thenReturn(member);
        when(loan.getStoredBook()).thenReturn(storedBook);
        when(storedBook.getBook()).thenReturn(book);

        Duration holdDuration = Duration.ofDays(3);
        when(reservationPolicy.holdDuration()).thenReturn(holdDuration);

        LocalDateTime dueDate = now.minusDays(2);
        when(loan.getDueDate()).thenReturn(dueDate);
        when(loan.isOverdue(now)).thenReturn(true);
        // when
        loanService.returnBook(loanId);
        // then
        verify(loan).returnBook(now, false);
        verify(book).assignHoldIfReservationExists(holdDuration, clock);
        verify(member, times(1)).applyPenalty(any(Penalty.class));
    }

    @Test
    @DisplayName("extend: 진행 중인 대출이고 타인 예약이 없으면 연장에 성공한다")
    void extendSuccess() {
        // given
        Long loanId = 1L;
        LocalDateTime now = LocalDateTime.of(2025, 11, 18, 10, 0);
        mockClock(now);

        Loan loan = mock(Loan.class);
        Member member = mock(Member.class);
        StoredBook storedBook = mock(StoredBook.class);
        Book book = mock(Book.class);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loan.isActive()).thenReturn(true);
        when(loan.getMember()).thenReturn(member);
        when(member.getId()).thenReturn(1L);
        when(loan.getStoredBook()).thenReturn(storedBook);
        when(storedBook.getBook()).thenReturn(book);

        when(book.hasActiveReservationByOtherMember(1L)).thenReturn(false);

        LocalDateTime currentDueDate = LocalDateTime.of(2025, 11, 25, 10, 0);
        LocalDateTime newDueDate = currentDueDate.plusDays(7);
        when(loan.getDueDate()).thenReturn(currentDueDate);
        when(loanPolicy.extendedDueDate(currentDueDate)).thenReturn(newDueDate);
        // when
        loanService.extend(loanId);
        // then
        verify(book).hasActiveReservationByOtherMember(1L);
        verify(loan).extend(now, false, newDueDate);
    }

    @Test
    @DisplayName("extend: 이미 반납된 대출이면 예외를 던진다")
    void extendThrowsWhenLoanNotActive() {
        // given
        Long loanId = 1L;
        Loan loan = mock(Loan.class);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loan.isActive()).thenReturn(false);
        // when & then
        assertThatThrownBy(() -> loanService.extend(loanId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("반납된 대출은 연장할 수 없습니다.");
    }
}
