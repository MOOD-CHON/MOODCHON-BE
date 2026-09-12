package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.request.CreateChonkangRequest;
import com.example.moodchon.domain.chonkangs.dto.response.CreateChonkangResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMember;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.mood.service.MoodCardResolver;
import com.example.moodchon.domain.place.entity.Post;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsCreateService {

    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMemberRepository chonkangsMemberRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final UserRepository userRepository;
    private final InviteCodeGenerator inviteCodeGenerator;
    private final TripDateValidator tripDateValidator;
    private final MoodCardResolver moodCardResolver;

    public CreateChonkangResponse create(Long hostId, CreateChonkangRequest request) {
        tripDateValidator.validate(request.startDate(), request.endDate());
        List<Post> selectedPosts = moodCardResolver.resolveExactlyThree(request.selectedMoodCardIds());

        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Chonkangs chonkang = chonkangsRepository.save(Chonkangs.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .plannedMemberCount(request.plannedMemberCount())
                .companionType(request.companionType())
                .travelMethod(request.travelMethod())
                .desiredRegion(request.desiredRegion())
                .accommodationConditions(request.accommodationConditions())
                .inviteCode(inviteCodeGenerator.generate())
                .host(host)
                .build());

        chonkangsMemberRepository.save(ChonkangsMember.builder()
                .chonkang(chonkang)
                .user(host)
                .build());

        for (Post post : selectedPosts) {
            chonkangsMoodSelectionRepository.save(ChonkangsMoodSelection.builder()
                    .chonkang(chonkang)
                    .user(host)
                    .post(post)
                    .build());
        }

        return CreateChonkangResponse.from(chonkang);
    }
}
