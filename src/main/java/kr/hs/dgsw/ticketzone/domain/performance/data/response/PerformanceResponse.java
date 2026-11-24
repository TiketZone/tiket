package kr.hs.dgsw.ticketzone.domain.performance.data.response;

import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;

import java.util.UUID;

public record PerformanceResponse(
        UUID id,
        String title,
        String content
) {
    public static PerformanceResponse from(Performance performance) {
        return new PerformanceResponse(
                performance.getId(),
                performance.getTitle(),
                performance.getContent()
        );
    }
}
