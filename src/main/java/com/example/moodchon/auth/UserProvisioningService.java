package com.example.moodchon.auth;

import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final UserCreator userCreator;

    // 최초 가입 시에만 소셜 프로필 값을 반영한다. 기존 회원의 닉네임/프로필 이미지는 PATCH
    // /api/users/me/nickname 등으로 직접 바꿀 수 있어서, 로그인마다 덮어쓰면 그 변경 사항이
    // 다음 로그인에 소셜 값(애플은 최초 로그인 이후 이메일이 없어 "Apple User")으로 되돌아간다.
    @Transactional
    public User saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .orElseGet(() -> createUser(attributes));
    }

    // 같은 소셜 계정으로 로그인 요청이 거의 동시에 두 번 들어오면, 위 조회 시점엔 둘 다
    // "없음"으로 통과했다가 저장 시점에 (provider, provider_id) unique 제약 위반이 날 수 있다.
    // 이미 다른 요청이 만들어 둔 유저를 다시 조회해서 반환해 로그인이 실패하지 않게 한다.
    private User createUser(OAuthAttributes attributes) {
        try {
            return userCreator.create(attributes);
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                    .orElseThrow(() -> e);
        }
    }
}
