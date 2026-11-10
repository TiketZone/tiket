package kr.hs.dgsw.ticketzone.domain.performance.exception;

import kr.hs.dgsw.ticketzone.global.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PerformanceStatusCode implements StatusCode {
    PERFORMANCE_NOT_FOUND("PERFORMANCE_NOT_FOUND", "공연을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
