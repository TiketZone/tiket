package kr.hs.dgsw.ticketzone.domain.ticket.controller;

import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;
import kr.hs.dgsw.ticketzone.domain.ticket.service.TicketService;
import kr.hs.dgsw.ticketzone.global.data.CommonResponse;
import kr.hs.dgsw.ticketzone.global.jwt.MemberDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<CommonResponse<TicketResponse>> issue(
            @RequestBody TicketRequest request,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return CommonResponse.ok("티켓 발권 성공", ticketService.issue(request, memberDetails.getMember().getId())).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<TicketResponse>>> getAll(
            @RequestParam("performance_id") UUID performanceId
    ) {
        return CommonResponse.ok("티켓 조회 성공", ticketService.getAll(performanceId)).toResponseEntity();
    }

    @GetMapping("/me")
    public ResponseEntity<CommonResponse<List<TicketResponse>>> getMyTickets(
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        return CommonResponse.ok("보유한 티켓을 조회했습니다.", ticketService.getMyTickets(memberDetails.getMember().getId())).toResponseEntity();
    }
}
