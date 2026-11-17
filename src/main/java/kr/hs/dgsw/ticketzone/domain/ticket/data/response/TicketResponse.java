package kr.hs.dgsw.ticketzone.domain.ticket.data.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID performanceId,
        String performanceTitle,
        LocalDateTime issuedAt
) {
    public static TicketResponse of(
            UUID id,
            UUID performanceId,
            String performanceTitle,
            LocalDateTime issuedAt
    ) {
        return new TicketResponse(
                id,
                performanceId,
                performanceTitle,
                issuedAt
        );
    }
}
