package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.request.UpdateChonkangInfoRequest;
import com.example.moodchon.domain.chonkangs.dto.response.UpdateChonkangInfoResponse;
import com.example.moodchon.domain.chonkangs.entity.AccommodationCondition;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMoodSelectionRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.recommendation.service.RecommendedItineraryResetService;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChonkangsInfoUpdateService {

    private final ChonkangsRepository chonkangsRepository;
    private final ChonkangsMoodSelectionRepository chonkangsMoodSelectionRepository;
    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final TripDateValidator tripDateValidator;
    private final RecommendedItineraryResetService recommendedItineraryResetService;

    public UpdateChonkangInfoResponse update(Long chonkangId, Long userId, UpdateChonkangInfoRequest request) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);
        tripDateValidator.validate(request.startDate(), request.endDate());

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        boolean otherFieldsChanged = hasOtherFieldsChanged(chonkang, request);

        if (!otherFieldsChanged && !request.retakeMood()) {
            chonkang.updateName(request.name());
            return new UpdateChonkangInfoResponse(false, false);
        }

        chonkang.updateTripInfo(
                request.name(), request.startDate(), request.endDate(), request.plannedMemberCount(),
                request.companionType(), request.travelMethod(), request.desiredRegion(),
                request.accommodationConditions());

        recommendedItineraryResetService.resetFor(chonkangId);

        if (request.retakeMood()) {
            chonkang.resetMood();
            chonkangsMoodSelectionRepository.deleteAllByChonkangId(chonkangId);
            return new UpdateChonkangInfoResponse(true, true);
        }

        return new UpdateChonkangInfoResponse(false, true);
    }

    private boolean hasOtherFieldsChanged(Chonkangs chonkang, UpdateChonkangInfoRequest request) {
        return !chonkang.getStartDate().equals(request.startDate())
                || !chonkang.getEndDate().equals(request.endDate())
                || chonkang.getPlannedMemberCount() != request.plannedMemberCount()
                || chonkang.getCompanionType() != request.companionType()
                || chonkang.getTravelMethod() != request.travelMethod()
                || chonkang.getDesiredRegion() != request.desiredRegion()
                || !normalize(chonkang.getAccommodationConditions()).equals(normalize(request.accommodationConditions()));
    }

    private Set<AccommodationCondition> normalize(Set<AccommodationCondition> conditions) {
        return conditions != null ? conditions : Set.of();
    }
}
