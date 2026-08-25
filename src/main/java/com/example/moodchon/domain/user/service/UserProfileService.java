package com.example.moodchon.domain.user.service;

import com.example.moodchon.domain.user.dto.request.UpdateNicknameRequest;
import com.example.moodchon.domain.user.dto.request.UpdateNotificationPreferenceRequest;
import com.example.moodchon.domain.user.dto.request.UpdateProfileImageRequest;
import com.example.moodchon.domain.user.dto.response.UserProfileResponse;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        return UserProfileResponse.from(findUser(userId));
    }

    public UserProfileResponse updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = findUser(userId);
        user.updateNickname(request.nickname());
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateProfileImage(Long userId, UpdateProfileImageRequest request) {
        User user = findUser(userId);
        user.updateProfileImageUrl(request.profileImageUrl());
        return UserProfileResponse.from(user);
    }

    public UserProfileResponse updateNotificationPreference(Long userId, UpdateNotificationPreferenceRequest request) {
        User user = findUser(userId);
        user.updateNotificationEnabled(request.enabled());
        return UserProfileResponse.from(user);
    }

    public void withdraw(Long userId) {
        User user = findUser(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
