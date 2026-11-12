package kr.hs.dgsw.ticketzone.domain.ticket.data.response;

import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        UUID performanceId,
        String performanceTitle,
        LocalDateTime issuedAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getPerformance().getId(),
                ticket.getPerformance().getTitle(),
                ticket.getIssuedAt()
        );
    }
}
