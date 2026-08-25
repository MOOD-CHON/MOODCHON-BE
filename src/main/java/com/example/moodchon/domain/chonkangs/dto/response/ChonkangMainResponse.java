package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.ChonkangsMainStatus;

public record ChonkangMainResponse(
        ChonkangsMainStatus status,
        MoodProgressResponse moodProgress,
        MoodResultResponse moodResult
) {

    public static ChonkangMainResponse moodVoting(MoodProgressResponse moodProgress) {
        return new ChonkangMainResponse(ChonkangsMainStatus.MOOD_VOTING, moodProgress, null);
    }

    public static ChonkangMainResponse moodDecided(MoodResultResponse moodResult) {
        return new ChonkangMainResponse(ChonkangsMainStatus.MOOD_DECIDED, null, moodResult);
    }
}
