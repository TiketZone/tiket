package kr.hs.dgsw.ticketzone.domain.performance.service;

import kr.hs.dgsw.ticketzone.domain.performance.data.request.PerformanceRequest;
import kr.hs.dgsw.ticketzone.domain.performance.data.response.PerformanceResponse;

import java.util.List;
import java.util.UUID;

public interface PerformanceService {
    PerformanceResponse create(PerformanceRequest request);
    List<PerformanceResponse> getAll();
    PerformanceResponse get(UUID id);
    PerformanceResponse update(UUID id, PerformanceRequest request);
    void delete(UUID id);
}
