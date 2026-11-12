package kr.hs.dgsw.ticketzone.domain.ticket.service.impl;

import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.domain.performance.repository.PerformanceRepository;
import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;
import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import kr.hs.dgsw.ticketzone.domain.ticket.repository.TicketRepository;
import kr.hs.dgsw.ticketzone.domain.ticket.service.TicketService;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final PerformanceRepository performanceRepository;

    @Override
    public TicketResponse issue(TicketRequest request) {
        Performance performance = performanceRepository.findNotDeletedById(request.performanceId())
                .orElseThrow(() -> new CommonException(PerformanceStatusCode.PERFORMANCE_NOT_FOUND));

        Ticket ticket = Ticket.builder()
                .performance(performance)
                .build();
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getAll(UUID performanceId) {
        performanceRepository.findNotDeletedById(performanceId)
                .orElseThrow(() -> new CommonException(PerformanceStatusCode.PERFORMANCE_NOT_FOUND));

        return ticketRepository.findAllByPerformanceId(performanceId).stream()
                .map(TicketResponse::from)
                .toList();
    }
}
