package pawparazzi.back.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.member.dto.KakaoUserDto;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${KAKAO_REDIRECT_URI}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * 카카오 액세스 토큰 요청
     */
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = new StringBuilder()
                .append("grant_type=authorization_code")
                .append("&client_id=").append(clientId)
                .append("&redirect_uri=").append(redirectUri)
                .append("&code=").append(code)
                .append("&client_secret=").append(clientSecret)
                .toString();

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("카카오 액세스 토큰 요청 실패: HTTP 상태 코드 {}", response.getStatusCode());
                throw new IllegalStateException("카카오 액세스 토큰 요청 실패");
            }

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("access_token").asText();

        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("카카오 액세스 토큰 요청 실패");
        }
    }

    /**
     * 카카오 사용자 정보 요청
     */
    public KakaoUserDto getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("카카오 사용자 정보 요청 실패: HTTP 상태 코드 {}", response.getStatusCode());
                throw new IllegalStateException("카카오 사용자 정보 요청 실패");
            }

            JsonNode userJson = objectMapper.readTree(response.getBody());

            return new KakaoUserDto(
                    userJson.get("id").asLong(),
                    userJson.get("kakao_account").get("email").asText(),
                    userJson.get("kakao_account").get("profile").get("nickname").asText(),
                    userJson.get("kakao_account").get("profile").get("profile_image_url").asText()
            );

        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 중 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("카카오 사용자 정보 요청 실패");
        }
    }
}