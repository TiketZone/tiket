package kr.hs.dgsw.ticketzone.domain.ticket.controller;

import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;
import kr.hs.dgsw.ticketzone.domain.ticket.service.TicketService;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import kr.hs.dgsw.ticketzone.global.handler.GlobalExceptionHandler;
import kr.hs.dgsw.ticketzone.global.jwt.MemberDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    private UUID memberId;

    @BeforeEach
    void setAuthenticatedMember() {
        memberId = UUID.randomUUID();
        Member member = Member.builder()
                .id(memberId)
                .username("test-user")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        MemberDetails memberDetails = new MemberDetails(member);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(memberDetails, null, memberDetails.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void issueReturnsIssuedTicket() throws Exception {
        UUID performanceId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        when(ticketService.issue(any(TicketRequest.class), any(UUID.class))).thenReturn(
                TicketResponse.of(
                        ticketId,
                        performanceId,
                        "콘서트",
                        LocalDateTime.of(2026, 11, 17, 15, 0)
                )
        );

        mockMvc.perform(post("/api/ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"performance_id":"%s"}
                                """.formatted(performanceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value("티켓 발권 성공"))
                .andExpect(jsonPath("$.data.id").value(ticketId.toString()))
                .andExpect(jsonPath("$.data.performanceId").value(performanceId.toString()))
                .andExpect(jsonPath("$.data.performanceTitle").value("콘서트"));
    }

    @Test
    void issueReturnsNotFoundWhenPerformanceDoesNotExist() throws Exception {
        when(ticketService.issue(any(TicketRequest.class), any(UUID.class)))
                .thenThrow(new CommonException(PerformanceStatusCode.PERFORMANCE_NOT_FOUND));

        mockMvc.perform(post("/api/ticket")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"performance_id":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpStatus").value(404))
                .andExpect(jsonPath("$.code").value("PERFORMANCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("공연을 찾을 수 없습니다."));
    }
}
