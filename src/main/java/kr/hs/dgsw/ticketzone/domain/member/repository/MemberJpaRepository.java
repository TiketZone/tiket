package kr.hs.dgsw.ticketzone.domain.member.repository;

import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);
}
