package smiinii.object_oriented_library.controller;

import org.springframework.web.bind.annotation.*;
import smiinii.object_oriented_library.dto.member.MemberCreateRequest;
import smiinii.object_oriented_library.dto.member.MemberCreateResponse;
import smiinii.object_oriented_library.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public MemberCreateResponse register(@RequestBody MemberCreateRequest request) {
        Long memberId = memberService.register(request.getName());
        return new MemberCreateResponse(memberId);
    }
}
