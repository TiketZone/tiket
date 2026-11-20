package kr.hs.dgsw.ticketzone.domain.member.data.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank @Size(min = 4, max = 20)
        String username,

        @NotBlank String password
) {
}
