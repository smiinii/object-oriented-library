package smiinii.object_oriented_library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import smiinii.object_oriented_library.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
