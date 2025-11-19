package smiinii.object_oriented_library.dto.member;

public class MemberCreateResponse {
    private final Long memberId;

    public MemberCreateResponse(Long memberId) {
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
