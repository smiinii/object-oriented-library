package smiinii.object_oriented_library.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("register: 회원을 생성하고 저장한 후 생성된 ID를 반환한다")
    void registerCreatesAndSavesMember() {
        // given
        String name = "이성민";

        Member saved = mock(Member.class);
        when(saved.getId()).thenReturn(1L);

        when(memberRepository.save(any(Member.class))).thenReturn(saved);
        // when
        Long id = memberService.register(name);
        // then
        assertThat(id).isEqualTo(1L);
        verify(memberRepository).save(any(Member.class));
    }
}
