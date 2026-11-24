package kr.hs.dgsw.ticketzone.global.handler;

import kr.hs.dgsw.ticketzone.global.data.CommonResponse;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonResponse<Void>> exceptionHandler(
            CommonException commonException
    ) {
        return CommonResponse
                .error(commonException)
                .toResponseEntity();
    }
}
