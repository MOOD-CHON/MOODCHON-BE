package com.example.moodchon.domain.accommodation.dto.response;

import com.example.moodchon.domain.accommodation.entity.AccommodationVote;

public record AccommodationVoterResponse(
        Long userId,
        String nickname,
        String profileImageUrl
) {

    public static AccommodationVoterResponse from(AccommodationVote vote) {
        return new AccommodationVoterResponse(
                vote.getUser().getId(),
                vote.getUser().getNickname(),
                vote.getUser().getProfileImageUrl()
        );
    }
}
