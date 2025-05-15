package pawparazzi.back.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.member.dto.KakaoUserDto;
import pawparazzi.back.member.service.KakaoAuthService;
import pawparazzi.back.member.service.MemberService;
import pawparazzi.back.member.entity.Member;
import pawparazzi.back.security.util.JwtUtil;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Value("${KAKAO_CLIENT_ID}")
    private String kakaoClientId;

    @Value("${KAKAO_REDIRECT_URI}")
    private String kakaoRedirectUri;

    /**
     * 클라이언트 ID 없이 카카오 로그인 URL로 자동 리디렉트
     */
    @GetMapping("/login/kakao")
    public ResponseEntity<Void> redirectToKakaoLogin() {
        String kakaoLoginUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code";

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(kakaoLoginUrl));
        return ResponseEntity.status(302).headers(headers).build();
    }

    /**
     * 카카오 로그인 콜백 (인가 코드 → JWT 발급)
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<Map<String, String>> kakaoLogin(@RequestParam String code) {
        try {
            String accessToken = kakaoAuthService.getAccessToken(code);
            KakaoUserDto kakaoUser = kakaoAuthService.getUserInfo(accessToken);
            Member member = memberService.handleKakaoLogin(kakaoUser);

            String jwtToken = jwtUtil.generateIdToken(member.getId());
            String refreshToken = memberService.generateOrUpdateRefreshToken(member);

            Map<String, String> tokenMap = Map.of(
                    "accessToken", jwtToken,
                    "refreshToken", refreshToken
            );

            return ResponseEntity.ok(tokenMap);

        } catch (IllegalStateException e) {
            log.error("카카오 인증 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("카카오 로그인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}