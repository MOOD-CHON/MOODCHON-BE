package com.example.moodchon.auth;

import com.example.moodchon.auth.dto.TokenResponse;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// 카카오 네이티브 SDK가 발급한 accessToken을 검증하는 로그인. kapi.kakao.com/v2/user/me 응답 형태는
// 기존 oauth2Login() 플로우의 DefaultOAuth2UserService가 받던 attributes와 동일해 OAuthAttributes.ofKakao()를 그대로 재사용한다.
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final UserProvisioningService userProvisioningService;
    private final TokenIssuer tokenIssuer;
    private final KakaoTokenExchanger kakaoTokenExchanger;
    private final RestClient restClient = RestClient.create();

    // 웹은 인가 코드만 받아오므로 서버에서 액세스 토큰으로 바꾼 뒤 기존 로그인 흐름을 그대로 탄다.
    public TokenResponse loginWithAuthorizationCode(String code, String redirectUri) {
        return login(kakaoTokenExchanger.exchangeForAccessToken(code, redirectUri));
    }

    public TokenResponse login(String accessToken) {
        Map<String, Object> attributes = fetchKakaoUser(accessToken);
        OAuthAttributes oAuthAttributes = OAuthAttributes.ofKakao(attributes);
        User user = userProvisioningService.saveOrUpdate(oAuthAttributes);

        return tokenIssuer.issue(user);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchKakaoUser(String accessToken) {
        try {
            return restClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
