package pawparazzi.back.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pawparazzi.back.member.dto.NaverUserDto;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverAuthService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${NAVER_CLIENT_ID}")
    private String clientId;

    @Value("${NAVER_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${NAVER_REDIRECT_URI}")
    private String redirectUri;

    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&code=" + code;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            log.error("네이버 액세스 토큰 요청 실패", e);
            throw new IllegalStateException("네이버 액세스 토큰 요청 실패");
        }
    }

    public NaverUserDto getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class
            );
            JsonNode data = objectMapper.readTree(response.getBody()).get("response");
            return new NaverUserDto(
                    data.get("id").asText(),
                    data.get("email").asText(),
                    data.get("name").asText(),
                    data.has("profile_image") ? data.get("profile_image").asText() : null
            );
        } catch (Exception e) {
            log.error("네이버 사용자 정보 요청 실패", e);
            throw new IllegalStateException("네이버 사용자 정보 요청 실패");
        }
    }
}