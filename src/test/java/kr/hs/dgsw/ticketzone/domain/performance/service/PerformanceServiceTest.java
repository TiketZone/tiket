package kr.hs.dgsw.ticketzone.domain.performance.service;

import kr.hs.dgsw.ticketzone.domain.performance.data.request.PerformanceRequest;
import kr.hs.dgsw.ticketzone.domain.performance.data.response.PerformanceResponse;
import kr.hs.dgsw.ticketzone.domain.performance.exception.PerformanceStatusCode;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PerformanceServiceTest {

    @Autowired
    private PerformanceService performanceService;

    @Test
    void createTest() {
        PerformanceResponse response = performanceService.create(new PerformanceRequest("공연", "공연 내용"));

        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("공연");
        assertThat(response.content()).isEqualTo("공연 내용");
    }

    @Test
    void getTest() {
        PerformanceResponse saved = performanceService.create(new PerformanceRequest("공연", "공연 내용"));

        PerformanceResponse response = performanceService.get(saved.id());

        assertThat(response.title()).isEqualTo("공연");
        assertThat(response.content()).isEqualTo("공연 내용");
    }

    @Test
    void getAllTest() {
        performanceService.create(new PerformanceRequest("공연1", "내용1"));
        performanceService.create(new PerformanceRequest("공연2", "내용2"));

        List<PerformanceResponse> list = performanceService.getAll();

        assertThat(list.size()).isEqualTo(2);
    }

    @Test
    void updateTest() {
        PerformanceResponse saved = performanceService.create(new PerformanceRequest("공연", "공연 내용"));

        performanceService.update(saved.id(), new PerformanceRequest("수정된 공연", "수정된 내용"));

        PerformanceResponse response = performanceService.get(saved.id());
        assertThat(response.title()).isEqualTo("수정된 공연");
        assertThat(response.content()).isEqualTo("수정된 내용");
    }

    @Test
    void deleteTest() {
        PerformanceResponse saved = performanceService.create(new PerformanceRequest("공연", "공연 내용"));

        performanceService.delete(saved.id());

        assertThat(performanceService.getAll().size()).isEqualTo(0);

        CommonException exception = null;
        try {
            performanceService.get(saved.id());
        } catch (CommonException e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
        assertThat(exception.statusCode).isEqualTo(PerformanceStatusCode.PERFORMANCE_NOT_FOUND);
    }

    @Test
    void getNotFoundTest() {
        CommonException exception = null;
        try {
            performanceService.get(UUID.randomUUID());
        } catch (CommonException e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
        assertThat(exception.statusCode).isEqualTo(PerformanceStatusCode.PERFORMANCE_NOT_FOUND);
    }
}
