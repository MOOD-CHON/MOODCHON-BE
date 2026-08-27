package com.example.moodchon.domain.chonkangs.dto.response;

import java.util.List;

public record MoodProgressResponse(
        int totalMemberCount,
        int completedMemberCount,
        List<MemberMoodProgressResponse> members
) {

    public static MoodProgressResponse of(List<MemberMoodProgressResponse> members) {
        long completedMemberCount = members.stream()
                .filter(MemberMoodProgressResponse::moodSelected)
                .count();
        return new MoodProgressResponse(members.size(), (int) completedMemberCount, members);
    }
}
