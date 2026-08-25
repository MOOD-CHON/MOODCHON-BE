package com.example.moodchon.domain.user.dto.response;

import com.example.moodchon.domain.user.entity.User;

public record UserProfileResponse(
        Long id,
        String nickname,
        String profileImageUrl,
        boolean notificationEnabled
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.isNotificationEnabled()
        );
    }
}
