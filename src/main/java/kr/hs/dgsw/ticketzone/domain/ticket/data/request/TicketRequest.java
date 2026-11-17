package kr.hs.dgsw.ticketzone.domain.ticket.data.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record TicketRequest(
        @JsonProperty("performance_id")
        UUID performanceId
) {
}
