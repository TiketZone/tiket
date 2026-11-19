package kr.hs.dgsw.ticketzone.domain.performance.service;

import kr.hs.dgsw.ticketzone.domain.performance.data.request.PerformanceRequest;
import kr.hs.dgsw.ticketzone.domain.performance.data.response.PerformanceResponse;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PerformanceServiceTest {

    @Autowired
    private PerformanceService performanceService;

    @Test
    void 공연_등록_후_조회() {
        PerformanceResponse created = performanceService.create(new PerformanceRequest("제목", "내용"));

        assertThat(performanceService.get(created.id())).isEqualTo(created);
        assertThat(performanceService.getAll()).contains(created);
    }

    @Test
    void 공연_수정() {
        UUID id = performanceService.create(new PerformanceRequest("제목", "내용")).id();

        PerformanceResponse updated = performanceService.update(id, new PerformanceRequest("새 제목", "새 내용"));

        assertThat(updated).isEqualTo(new PerformanceResponse(id, "새 제목", "새 내용"));
        assertThat(performanceService.get(id)).isEqualTo(updated);
    }

    @Test
    void 삭제된_공연은_조회되지_않음() {
        PerformanceResponse created = performanceService.create(new PerformanceRequest("제목", "내용"));

        performanceService.delete(created.id());

        assertThat(performanceService.getAll()).doesNotContain(created);
        assertNotFound(created.id());
    }

    @Test
    void 없는_공연은_NOT_FOUND() {
        assertNotFound(UUID.randomUUID());
    }

    private void assertNotFound(UUID id) {
        assertThatThrownBy(() -> performanceService.get(id))
                .isInstanceOf(CommonException.class)
                .extracting("statusCode")
                .isEqualTo(PerformanceStatusCode.PERFORMANCE_NOT_FOUND);
    }
}
