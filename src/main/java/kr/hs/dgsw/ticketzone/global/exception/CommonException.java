package kr.hs.dgsw.ticketzone.global.exception;

public class CommonException extends RuntimeException {

    public final StatusCode statusCode;

    public CommonException(StatusCode statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
    }
}
