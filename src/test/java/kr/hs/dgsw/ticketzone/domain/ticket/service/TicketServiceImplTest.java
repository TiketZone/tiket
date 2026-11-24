package kr.hs.dgsw.ticketzone.domain.ticket.service;

import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.member.exception.MemberStatusCode;
import kr.hs.dgsw.ticketzone.domain.member.repository.MemberJpaRepository;
import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.domain.performance.repository.PerformanceRepository;
import kr.hs.dgsw.ticketzone.domain.ticket.data.request.TicketRequest;
import kr.hs.dgsw.ticketzone.domain.ticket.data.response.TicketResponse;
import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import kr.hs.dgsw.ticketzone.domain.ticket.repository.TicketRepository;
import kr.hs.dgsw.ticketzone.domain.ticket.service.impl.TicketServiceImpl;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock TicketRepository ticketRepository;
    @Mock PerformanceRepository performanceRepository;
    @Mock MemberJpaRepository memberRepository;

    @InjectMocks TicketServiceImpl ticketService;

    private static final UUID MEMBER_ID = UUID.randomUUID();
    private static final UUID PERFORMANCE_ID = UUID.randomUUID();

    private Member member() {
        Member member = Member.builder().username("test1").password("encoded").role(Role.ROLE_USER).build();
        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
        return member;
    }

    private Performance performance() {
        Performance performance = Performance.builder().title("공연").content("내용").build();
        ReflectionTestUtils.setField(performance, "id", PERFORMANCE_ID);
        return performance;
    }

    private void assertStatus(Throwable e, Object code) {
        assertThat(e).isInstanceOf(CommonException.class);
        assertThat(((CommonException) e).statusCode).isEqualTo(code);
    }

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("when success, save ticket linked to member and performance")
        void success() {
            // given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member()));
            given(performanceRepository.findNotDeletedById(PERFORMANCE_ID)).willReturn(Optional.of(performance()));
            given(ticketRepository.save(any(Ticket.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            TicketResponse response = ticketService.issue(new TicketRequest(PERFORMANCE_ID), MEMBER_ID);

            // then
            ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
            then(ticketRepository).should().save(captor.capture());
            Ticket saved = captor.getValue();
            assertThat(saved.getMember().getId()).isEqualTo(MEMBER_ID);
            assertThat(saved.getPerformance().getId()).isEqualTo(PERFORMANCE_ID);
            assertThat(saved.getIssuedAt()).isNotNull();
            assertThat(response.performanceId()).isEqualTo(PERFORMANCE_ID);
            assertThat(response.performanceTitle()).isEqualTo("공연");
        }

        @Test
        @DisplayName("when member not found, throw MEMBER_NOT_FOUND")
        void memberNotFound() {
            // given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            // when
            Throwable e = catchThrowable(() -> ticketService.issue(new TicketRequest(PERFORMANCE_ID), MEMBER_ID));

            // then
            assertStatus(e, MemberStatusCode.MEMBER_NOT_FOUND);
            then(ticketRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("when performance not found or deleted, throw PERFORMANCE_NOT_FOUND")
        void performanceNotFound() {
            // given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member()));
            given(performanceRepository.findNotDeletedById(PERFORMANCE_ID)).willReturn(Optional.empty());

            // when
            Throwable e = catchThrowable(() -> ticketService.issue(new TicketRequest(PERFORMANCE_ID), MEMBER_ID));

            // then
            assertStatus(e, PerformanceStatusCode.PERFORMANCE_NOT_FOUND);
            then(ticketRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("getMyTickets")
    class GetMyTickets {

        @Test
        @DisplayName("return tickets of the member")
        void success() {
            // given
            Ticket ticket = Ticket.builder().performance(performance()).member(member()).build();
            given(ticketRepository.findAllByMemberId(MEMBER_ID)).willReturn(List.of(ticket));

            // when
            List<TicketResponse> responses = ticketService.getMyTickets(MEMBER_ID);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).performanceId()).isEqualTo(PERFORMANCE_ID);
        }

        @Test
        @DisplayName("when no tickets, return empty list")
        void empty() {
            // given
            given(ticketRepository.findAllByMemberId(MEMBER_ID)).willReturn(List.of());

            // when & then
            assertThat(ticketService.getMyTickets(MEMBER_ID)).isEmpty();
        }
    }
}
