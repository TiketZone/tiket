package kr.hs.dgsw.ticketzone.domain.ticket.repository;

import jakarta.persistence.EntityManager;
import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import kr.hs.dgsw.ticketzone.domain.ticket.entity.Ticket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TicketRepositoryTest {

    @Autowired TicketRepository ticketRepository;
    @Autowired EntityManager em;

    private Member saveMember(String username) {
        Member member = Member.builder().username(username).password("encoded").role(Role.ROLE_USER).build();
        em.persist(member);
        return member;
    }

    private Performance savePerformance(String title) {
        Performance performance = Performance.builder().title(title).content("내용").build();
        em.persist(performance);
        return performance;
    }

    @Test
    @DisplayName("findAllByMemberId returns only the member's tickets")
    void findAllByMemberId() {
        // given
        Member me = saveMember("me");
        Member other = saveMember("other");
        Performance performance = savePerformance("공연");
        ticketRepository.save(Ticket.builder().member(me).performance(performance).build());
        ticketRepository.save(Ticket.builder().member(me).performance(performance).build());
        ticketRepository.save(Ticket.builder().member(other).performance(performance).build());
        em.flush();
        em.clear();

        // when
        List<Ticket> tickets = ticketRepository.findAllByMemberId(me.getId());

        // then
        assertThat(tickets).hasSize(2)
                .allSatisfy(t -> assertThat(t.getMember().getId()).isEqualTo(me.getId()));
        assertThat(tickets.get(0).getPerformance().getTitle()).isEqualTo("공연");
    }

    @Test
    @DisplayName("saving a ticket without member violates NOT NULL constraint")
    void memberRequired() {
        // given
        Performance performance = savePerformance("공연");

        // when & then
        assertThatThrownBy(() -> {
            ticketRepository.save(Ticket.builder().performance(performance).build());
            em.flush();
        }).isInstanceOf(Exception.class);
    }
}
