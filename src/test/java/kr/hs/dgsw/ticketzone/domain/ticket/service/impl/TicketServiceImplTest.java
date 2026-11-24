package kr.hs.dgsw.ticketzone.domain.ticket.service.impl;

import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.member.repository.MemberJpaRepository;
import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.domain.performance.repository.PerformanceRepository;
import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;
import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import kr.hs.dgsw.ticketzone.domain.ticket.repository.TicketRepository;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private MemberJpaRepository memberRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void getAllReturnsTicketsForPerformance() {
        UUID performanceId = UUID.randomUUID();
        Performance performance = Performance.builder()
                .id(performanceId)
                .title("콘서트")
                .build();
        when(performanceRepository.findNotDeletedById(performanceId))
                .thenReturn(Optional.of(performance));
        when(ticketRepository.findAllByPerformanceId(performanceId))
                .thenReturn(List.of(
                        Ticket.builder().performance(performance).build(),
                        Ticket.builder().performance(performance).build()
                ));

        List<TicketResponse> responses = ticketService.getAll(performanceId);

        assertThat(responses).hasSize(2)
                .allSatisfy(response -> {
                    assertThat(response.performanceId()).isEqualTo(performanceId);
                    assertThat(response.performanceTitle()).isEqualTo("콘서트");
                    assertThat(response.issuedAt()).isNotNull();
                });
        verify(ticketRepository).findAllByPerformanceId(performanceId);
    }

    @Test
    void issueThrowsWhenPerformanceDoesNotExist() {
        UUID performanceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member(memberId)));
        when(performanceRepository.findNotDeletedById(performanceId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.issue(new TicketRequest(performanceId), memberId))
                .isInstanceOfSatisfying(CommonException.class, exception ->
                        assertThat(exception.statusCode)
                                .isEqualTo(PerformanceStatusCode.PERFORMANCE_NOT_FOUND)
                );

        verifyNoInteractions(ticketRepository);
    }

    @Test
    void issueSavesTicketForExistingPerformance() {
        UUID performanceId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        Performance performance = Performance.builder()
                .id(performanceId)
                .title("콘서트")
                .build();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member(memberId)));
        when(performanceRepository.findNotDeletedById(performanceId))
                .thenReturn(Optional.of(performance));
        when(ticketRepository.save(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.issue(new TicketRequest(performanceId), memberId);

        assertThat(response.performanceId()).isEqualTo(performanceId);
        assertThat(response.performanceTitle()).isEqualTo("콘서트");
        assertThat(response.issuedAt()).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
    }

    private Member member(UUID memberId) {
        Member member = Member.builder()
                .username("test-user")
                .password("encoded-password")
                .role(Role.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}
