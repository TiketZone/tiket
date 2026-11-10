package kr.hs.dgsw.ticketzone.domain.performance.service.impl;

import kr.hs.dgsw.ticketzone.domain.performance.data.request.PerformanceRequest;
import kr.hs.dgsw.ticketzone.domain.performance.data.response.PerformanceResponse;
import kr.hs.dgsw.ticketzone.domain.performance.entity.Performance;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.domain.performance.repository.PerformanceRepository;
import kr.hs.dgsw.ticketzone.domain.performance.service.PerformanceService;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;

    @Override
    @Transactional
    public PerformanceResponse create(PerformanceRequest request) {
        Performance performance = Performance.builder()
                .title(request.title())
                .content(request.content())
                .build();
        return PerformanceResponse.from(performanceRepository.save(performance));
    }

    @Override
    public List<PerformanceResponse> getAll() {
        return performanceRepository.findAllNotDeleted().stream()
                .map(PerformanceResponse::from)
                .toList();
    }

    @Override
    public PerformanceResponse get(UUID id) {
        return PerformanceResponse.from(findPerformance(id));
    }

    @Override
    @Transactional
    public PerformanceResponse update(UUID id, PerformanceRequest request) {
        Performance performance = findPerformance(id);
        performance.update(request.title(), request.content());
        return PerformanceResponse.from(performance);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        findPerformance(id).delete();
    }

    private Performance findPerformance(UUID id) {
        return performanceRepository.findNotDeletedById(id)
                .orElseThrow(() -> new CommonException(PerformanceStatusCode.PERFORMANCE_NOT_FOUND));
    }
}
