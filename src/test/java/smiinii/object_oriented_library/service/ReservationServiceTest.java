package smiinii.object_oriented_library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.domain.reservation.Reservation;
import smiinii.object_oriented_library.domain.reservation_policy.ReservationPolicy;
import smiinii.object_oriented_library.repository.BookRepository;
import smiinii.object_oriented_library.repository.MemberRepository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReservationPolicy reservationPolicy;

    @Mock
    private Clock clock;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                memberRepository,
                bookRepository,
                reservationPolicy,
                clock
        );
    }

    @Test
    @DisplayName("reserve: 정상적인 회원과 도서라면 예약에 성공한다")
    void reserveSuccess() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Member member = mock(Member.class);
        Book book = mock(Book.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.canBorrow()).thenReturn(true);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationPolicy.maxQueueSize()).thenReturn(3);
        when(book.tryReserve(memberId, 3, clock)).thenReturn(true);
        // when
        reservationService.reserve(memberId, bookId);
        // then
        verify(memberRepository, times(1)).findById(memberId);
        verify(member, times(1)).releasePenaltyIfExpired(clock);
        verify(member, times(1)).canBorrow();

        verify(bookRepository, times(1)).findById(bookId);
        verify(book, times(1)).tryReserve(memberId, 3, clock);
    }

    @Test
    @DisplayName("reserve: 존재하지 않는 회원이면 예외를 던진다")
    void reserveThrowsWhenMemberNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reservationService.reserve(memberId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");

        verify(memberRepository, times(1)).findById(memberId);
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("reserve: 대출/예약이 불가능한 회원이면 예외를 던진다")
    void reserveThrowsWhenMemberCannotBorrow() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Member member = mock(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.canBorrow()).thenReturn(false);
        // when & then
        assertThatThrownBy(() -> reservationService.reserve(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("현재 대출/예약이 불가능한 회원입니다.");

        verify(memberRepository, times(1)).findById(memberId);
        verify(member, times(1)).releasePenaltyIfExpired(clock);
        verify(member, times(1)).canBorrow();
        verifyNoInteractions(bookRepository);
    }

    @Test
    @DisplayName("reserve: 존재하지 않는 도서면 예외를 던진다")
    void reserveThrowsWhenBookNotFound() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Member member = mock(Member.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.canBorrow()).thenReturn(true);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reservationService.reserve(memberId, bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 도서입니다.");

        verify(memberRepository, times(1)).findById(memberId);
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("reserve: 이미 활성 예약이 있거나 대기열이 꽉 찼으면 예외를 던진다")
    void reserveThrowsWhenReservationFailed() {
        // given
        Long memberId = 1L;
        Long bookId = 10L;
        Member member = mock(Member.class);
        Book book = mock(Book.class);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(member.canBorrow()).thenReturn(true);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(reservationPolicy.maxQueueSize()).thenReturn(3);
        when(book.tryReserve(memberId, 3, clock)).thenReturn(false);
        // when & then
        assertThatThrownBy(() -> reservationService.reserve(memberId, bookId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 활성 예약이 있거나 예약 대기열이 가득 찼습니다.");

        verify(book, times(1)).tryReserve(memberId, 3, clock);
    }

    @Test
    @DisplayName("getReservationsForBook: 도서의 예약 목록을 조회할 수 있다")
    void getReservationsForBookReturnsReservations() {
        // given
        Long bookId = 10L;
        Book book = mock(Book.class);
        List<Reservation> reservations = List.of(
                mock(Reservation.class),
                mock(Reservation.class)
        );

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(book.getReservations()).thenReturn(reservations);
        // when
        List<Reservation> result = reservationService.getReservationsForBook(bookId);
        // then
        assertThat(result).isSameAs(reservations);
        verify(bookRepository, times(1)).findById(bookId);
        verify(book, times(1)).getReservations();
    }

    @Test
    @DisplayName("getReservationsForBook: 존재하지 않는 도서면 예외를 던진다")
    void getReservationsForBookThrowsWhenBookNotFound() {
        // given
        Long bookId = 10L;
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> reservationService.getReservationsForBook(bookId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 도서입니다.");

        verify(bookRepository, times(1)).findById(bookId);
    }
}
