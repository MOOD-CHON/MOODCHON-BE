package com.example.moodchon.auth;

import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

// 탈퇴 시 카카오 쪽 앱 연결(동의)도 끊어서, 재가입 후 다시 로그인해도 예전 동의가
// 재사용되지 않게 한다. Admin 키로 호출하므로 사용자의 로그인 세션과 무관하게 실행할 수 있다.
@Service
public class KakaoUnlinkService {

    private final RestClient restClient = RestClient.create();
    private final String adminKey;

    public KakaoUnlinkService(@Value("${kakao.admin-key}") String adminKey) {
        this.adminKey = adminKey;
    }

    public void unlink(String kakaoUserId) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", kakaoUserId);

        try {
            restClient.post()
                    .uri("https://kapi.kakao.com/v1/user/unlink")
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_FAILED);
        }
    }
}
