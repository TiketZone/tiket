package kr.hs.dgsw.ticketzone.domain.member.data.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueRequest(
        @NotBlank String refreshToken
) {
}
