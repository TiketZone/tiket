package kr.hs.dgsw.ticketzone.domain.member.service;

import kr.hs.dgsw.ticketzone.domain.member.data.request.ReissueRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignInRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignUpRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignInResponse;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignUpResponse;

public interface MemberService {

    SignUpResponse signUp(SignUpRequest request);

    SignInResponse signIn(SignInRequest request);

    SignInResponse reissue(ReissueRequest request);

    void signOut(String username, String accessToken);
}
