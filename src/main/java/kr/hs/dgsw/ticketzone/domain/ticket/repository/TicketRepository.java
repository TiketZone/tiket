package kr.hs.dgsw.ticketzone.domain.ticket.repository;

import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.performance p WHERE p.id = :performanceId")
    List<Ticket> findAllByPerformanceId(@Param("performanceId") UUID performanceId);
}
