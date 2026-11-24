package kr.hs.dgsw.ticketzone.global.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonResponse<T> (
    int httpStatus,
    String message,
    String code,
    T data
) {
    public static <T> CommonResponse<T> of(
            HttpStatus httpStatus,
            String message,
            String code,
            T data
    ) {
        return new CommonResponse<>(
                httpStatus.value(),
                message,
                code,
                data
        );
    }

    // 성공 응답(반환 데이터가 포함된 경우)
    public static <T> CommonResponse<T> ok(
            String message,
            T data
    ) {
        return CommonResponse.of(
                HttpStatus.OK,
                message,
                null,
                data
        );
    }

    // 성공 응답(반환 데이터가 없는 경우)
    public static CommonResponse<Void> ok(
            String message
    ) {
        return CommonResponse.of(
                HttpStatus.OK,
                message,
                null,
                null
        );
    }

    // 에러 응답
    public static CommonResponse<Void> error(
            CommonException commonException
    ) {
        return CommonResponse.of(
                commonException.statusCode.getHttpStatus(),
                commonException.statusCode.getMessage(),
                commonException.statusCode.getCode(),
                null
        );
    }

    // ResponseEntity 타입으로 변경
    public ResponseEntity<CommonResponse<T>> toResponseEntity() {
        return ResponseEntity
                .status(httpStatus)
                .body(this);
    }
}