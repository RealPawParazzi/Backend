package pawparazzi.back.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pawparazzi.back.member.dto.NaverUserDto;
import pawparazzi.back.member.service.NaverAuthService;
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
public class NaverAuthController {

    private final NaverAuthService naverAuthService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Value("${NAVER_CLIENT_ID}")
    private String naverClientId;

    @Value("${NAVER_REDIRECT_URI}")
    private String naverRedirectUri;

    @GetMapping("/login/naver")
    public ResponseEntity<Void> redirectToNaverLogin() {
        String naverLoginUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + naverClientId
                + "&redirect_uri=" + naverRedirectUri
                + "&state=secureRandomState";

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(naverLoginUrl));
        return ResponseEntity.status(302).headers(headers).build();
    }

    @GetMapping("/naver/callback")
    public ResponseEntity<Map<String, String>> naverLogin(@RequestParam String code, @RequestParam String state) {
        try {
            String accessToken = naverAuthService.getAccessToken(code);
            NaverUserDto naverUser = naverAuthService.getUserInfo(accessToken);
            Member member = memberService.handleNaverLogin(naverUser); // 이 메서드 구현 필요

            String jwtToken = jwtUtil.generateIdToken(member.getId());
            String refreshToken = memberService.generateOrUpdateRefreshToken(member);

            Map<String, String> tokenMap = Map.of(
                    "accessToken", jwtToken,
                    "refreshToken", refreshToken
            );

            return ResponseEntity.ok(tokenMap);

        } catch (IllegalStateException e) {
            log.error("네이버 인증 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("네이버 로그인 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}