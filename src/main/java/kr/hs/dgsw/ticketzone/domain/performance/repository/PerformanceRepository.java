package kr.hs.dgsw.ticketzone.domain.performance.repository;

import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {

    List<Performance> findAllByDeletedFalse();

    Optional<Performance> findByIdAndDeletedFalse(UUID id);
}
