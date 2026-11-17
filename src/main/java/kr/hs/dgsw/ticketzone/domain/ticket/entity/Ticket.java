package kr.hs.dgsw.ticketzone.domain.ticket.entity;

import jakarta.persistence.*;
import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();
}
