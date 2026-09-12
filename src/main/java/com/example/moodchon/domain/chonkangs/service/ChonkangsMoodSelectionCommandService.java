package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.request.SubmitMoodSelectionRequest;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.entity.ChonkangsMoodSelection;
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
public class ChonkangsMoodSelectionCommandService {

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final UserRepository userRepository;
    private final MoodCardResolver moodCardResolver;
    private final ChonkangsMoodResultService chonkangsMoodResultService;

    public void submit(Long chonkangId, Long userId, SubmitMoodSelectionRequest request) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        List<Post> selectedPosts = moodCardResolver.resolveExactlyThree(request.selectedMoodCardIds());

        chonkangsMoodSelectionRepository.deleteAllByChonkangIdAndUserId(chonkangId, userId);
        // Hibernate는 한 번의 flush에서 INSERT를 DELETE보다 먼저 실행한다. 여기서 강제로 flush 하지 않으면
        // 같은 사진을 다시 고른 재제출이 (chonkang_id, user_id, post_id) 유니크 제약에 걸린다.
        chonkangsMoodSelectionRepository.flush();

        for (Post post : selectedPosts) {
            chonkangsMoodSelectionRepository.save(ChonkangsMoodSelection.builder()
                    .chonkang(chonkang)
                    .user(user)
                    .post(post)
                    .build());
        }

        chonkangsMoodResultService.confirmIfAllMembersSubmitted(chonkang);
    }
}
