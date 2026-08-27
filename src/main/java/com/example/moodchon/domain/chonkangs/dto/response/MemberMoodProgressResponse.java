package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;

public record MemberMoodProgressResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        boolean moodSelected
) {

    public static MemberMoodProgressResponse of(ChonkangsMember member, boolean moodSelected) {
        return new MemberMoodProgressResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                moodSelected
        );
    }
}
