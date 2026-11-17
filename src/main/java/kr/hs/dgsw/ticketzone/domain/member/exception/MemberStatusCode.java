package kr.hs.dgsw.ticketzone.domain.member.exception;

import kr.hs.dgsw.ticketzone.global.exception.StatusCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberStatusCode implements StatusCode {

    USERNAME_ALREADY_EXIST("USERNAME_ALREADY_EXIST", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    PASSWORD_IS_WEAK("PASSWORD_IS_WEAK", "비밀번호는 8자 이상이며 영문 대소문자, 숫자, 특수문자를 포함해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
