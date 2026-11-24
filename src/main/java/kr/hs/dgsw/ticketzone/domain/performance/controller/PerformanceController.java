package kr.hs.dgsw.ticketzone.domain.performance.controller;

import kr.hs.dgsw.ticketzone.domain.performance.data.request.PerformanceRequest;
import kr.hs.dgsw.ticketzone.domain.performance.data.response.PerformanceResponse;
import kr.hs.dgsw.ticketzone.domain.performance.service.PerformanceService;
import kr.hs.dgsw.ticketzone.global.data.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping
    public ResponseEntity<CommonResponse<PerformanceResponse>> create(@RequestBody PerformanceRequest request) {
        return CommonResponse.ok("공연 등록 성공", performanceService.create(request)).toResponseEntity();
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<PerformanceResponse>>> getAll() {
        return CommonResponse.ok("공연 목록 조회 성공", performanceService.getAll()).toResponseEntity();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PerformanceResponse>> get(@PathVariable UUID id) {
        return CommonResponse.ok("공연 조회 성공", performanceService.get(id)).toResponseEntity();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<PerformanceResponse>> update(
            @PathVariable UUID id,
            @RequestBody PerformanceRequest request
    ) {
        return CommonResponse.ok("공연 수정 성공", performanceService.update(id, request)).toResponseEntity();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(@PathVariable UUID id) {
        performanceService.delete(id);
        return CommonResponse.ok("공연 삭제 성공").toResponseEntity();
    }
}
