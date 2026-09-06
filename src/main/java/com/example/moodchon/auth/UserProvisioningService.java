package com.example.moodchon.auth;

import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;

    // 최초 가입 시에만 소셜 프로필 값을 반영한다. 기존 회원의 닉네임/프로필 이미지는 PATCH
    // /api/users/me/nickname 등으로 직접 바꿀 수 있어서, 로그인마다 덮어쓰면 그 변경 사항이
    // 다음 로그인에 소셜 값(애플은 최초 로그인 이후 이메일이 없어 "Apple User")으로 되돌아간다.
    @Transactional
    public User saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
    }
}
