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

    @Transactional
    public User saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findByProviderAndProviderId(attributes.getProvider(), attributes.getProviderId())
                .map(user -> {
                    user.updateProfile(attributes.getNickname(), attributes.getProfileImageUrl());
                    return user;
                })
                .orElseGet(() -> userRepository.save(attributes.toEntity()));
    }
}
