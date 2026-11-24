package kr.hs.dgsw.ticketzone.global.exception;

import org.springframework.http.HttpStatus;

public interface StatusCode {
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
