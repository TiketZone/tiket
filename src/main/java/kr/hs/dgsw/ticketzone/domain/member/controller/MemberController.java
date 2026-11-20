package kr.hs.dgsw.ticketzone.domain.member.controller;

import kr.hs.dgsw.ticketzone.domain.member.data.request.ReissueRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignInRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignUpRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignInResponse;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignUpResponse;
import kr.hs.dgsw.ticketzone.domain.member.service.MemberService;
import kr.hs.dgsw.ticketzone.global.data.CommonResponse;
import kr.hs.dgsw.ticketzone.global.jwt.JwtProvider;
import kr.hs.dgsw.ticketzone.global.jwt.MemberDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<SignUpResponse>> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        return CommonResponse.ok(
                "회원가입에 성공했습니다.",
                        memberService.signUp(signUpRequest)
                ).toResponseEntity();
    }

    @PostMapping("/signin")
    public ResponseEntity<CommonResponse<SignInResponse>> signIn(
            @Valid @RequestBody SignInRequest signInRequest
    ) {
        return CommonResponse.ok(
                "로그인에 성공했습니다.",
                        memberService.signIn(signInRequest)
                ).toResponseEntity();
    }

    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<SignInResponse>> reissue(
            @Valid @RequestBody ReissueRequest request
    ) {
        return CommonResponse
                .ok("토큰이 재발급되었습니다.", memberService.reissue(request))
                .toResponseEntity();
    }

    @PostMapping("/signout")
    public ResponseEntity<CommonResponse<Void>> signOut(
            @AuthenticationPrincipal MemberDetails memberDetails,
            HttpServletRequest request
    ) {
        memberService.signOut(memberDetails.getUsername(), jwtProvider.resolveToken(request));
        return CommonResponse
                .ok("로그아웃되었습니다.")
                .toResponseEntity();
    }
}
