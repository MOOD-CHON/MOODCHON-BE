package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;

public record ChonkangMemberResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        boolean isHost
) {

    public static ChonkangMemberResponse of(ChonkangsMember member, Long hostId) {
        return new ChonkangMemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getUser().getId().equals(hostId)
        );
    }
}
