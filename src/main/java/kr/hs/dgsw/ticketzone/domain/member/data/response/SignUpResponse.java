package kr.hs.dgsw.ticketzone.domain.member.data.response;

import kr.hs.dgsw.ticketzone.domain.member.entity.Member;

import java.util.UUID;

public record SignUpResponse(
        UUID id,
        String username
) {
    public static SignUpResponse of(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getUsername()
        );
    }
}
