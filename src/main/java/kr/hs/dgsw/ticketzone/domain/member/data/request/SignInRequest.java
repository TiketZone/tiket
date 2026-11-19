package kr.hs.dgsw.ticketzone.domain.member.data.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
