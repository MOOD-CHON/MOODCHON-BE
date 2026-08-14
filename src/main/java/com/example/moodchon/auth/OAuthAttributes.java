package com.example.moodchon.auth;

import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.entity.UserRole;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {

    private final Long kakaoId;
    private final String nickname;
    private final String profileImageUrl;

    @Builder
    private OAuthAttributes(Long kakaoId, String nickname, String profileImageUrl) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    @SuppressWarnings("unchecked")
    public static OAuthAttributes of(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .kakaoId(Long.valueOf(String.valueOf(attributes.get("id"))))
                .nickname((String) profile.get("nickname"))
                .profileImageUrl((String) profile.get("profile_image_url"))
                .build();
    }

    public User toEntity() {
        return User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(UserRole.USER)
                .build();
    }
}