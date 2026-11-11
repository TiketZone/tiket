package kr.hs.dgsw.ticketzone.domain.ticket.repository;

import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
}
