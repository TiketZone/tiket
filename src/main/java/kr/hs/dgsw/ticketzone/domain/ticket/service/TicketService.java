package kr.hs.dgsw.ticketzone.domain.ticket.service;

import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;

public interface TicketService {
    TicketResponse issue(TicketRequest request);
}
