package kr.hs.dgsw.ticketzone.domain.performance.repository;

import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerformanceRepository extends JpaRepository<Performance, UUID> {

    @Query("SELECT p FROM Performance p WHERE p.deleted = false")
    List<Performance> findAllNotDeleted();

    @Query("SELECT p FROM Performance p WHERE p.id = :id AND p.deleted = false")
    Optional<Performance> findNotDeletedById(@Param("id") UUID id);
}
