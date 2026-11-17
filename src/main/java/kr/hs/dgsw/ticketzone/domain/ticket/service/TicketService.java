package kr.hs.dgsw.ticketzone.domain.ticket.service;

import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;

import java.util.List;
import java.util.UUID;

public interface TicketService {
    TicketResponse issue(TicketRequest request);
    List<TicketResponse> getAll(UUID performanceId);
}
