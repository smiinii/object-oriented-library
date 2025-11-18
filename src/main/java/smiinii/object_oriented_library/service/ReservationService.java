package smiinii.object_oriented_library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smiinii.object_oriented_library.domain.Book;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.domain.reservation.Reservation;
import smiinii.object_oriented_library.domain.reservation_policy.ReservationPolicy;
import smiinii.object_oriented_library.repository.BookRepository;
import smiinii.object_oriented_library.repository.MemberRepository;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ReservationPolicy reservationPolicy;
    private final Clock clock;

    public ReservationService(
            MemberRepository memberRepository,
            BookRepository bookRepository,
            ReservationPolicy reservationPolicy,
            Clock clock
    ) {
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.reservationPolicy = reservationPolicy;
        this.clock = clock;
    }

    @Transactional
    public void reserve(Long memberId, Long bookId) {
        validateMember(memberId);
        Book book = findBook(bookId);

        boolean success = book.tryReserve(memberId, reservationPolicy.maxQueueSize(), clock);

        if (!success) {
            throw new IllegalStateException("이미 활성 예약이 있거나 예약 대기열이 가득 찼습니다.");
        }
    }

    public List<Reservation> getReservationsForBook(Long bookId) {
        Book book = findBook(bookId);
        return book.getReservations();
    }

    private void validateMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.releasePenaltyIfExpired(clock);

        if (!member.canBorrow()) {
            throw new IllegalStateException("현재 대출/예약이 불가능한 회원입니다.");
        }
    }

    private Book findBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 도서입니다."));
    }
}
