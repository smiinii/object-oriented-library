package smiinii.object_oriented_library.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smiinii.object_oriented_library.domain.Member;
import smiinii.object_oriented_library.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long register(String name) {
        Member member = Member.create(name);
        Member saved = memberRepository.save(member);
        return saved.getId();
    }
}
