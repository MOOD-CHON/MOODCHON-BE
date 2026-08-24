package com.example.moodchon.domain.chonkangs.dto.response;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;

public record CreateChonkangResponse(
        Long chonkangId,
        String inviteCode,
        int plannedMemberCount
) {

    public static CreateChonkangResponse from(Chonkangs chonkang) {
        return new CreateChonkangResponse(chonkang.getId(), chonkang.getInviteCode(), chonkang.getPlannedMemberCount());
    }
}
