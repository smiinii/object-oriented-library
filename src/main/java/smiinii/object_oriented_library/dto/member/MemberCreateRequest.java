package smiinii.object_oriented_library.dto.member;

public class MemberCreateRequest {

    private String name;

    public MemberCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
