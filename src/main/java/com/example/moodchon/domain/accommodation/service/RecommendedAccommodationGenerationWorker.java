package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.ai.AccommodationCandidate;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchResult;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodation;
import com.example.moodchon.domain.accommodation.entity.RecommendedAccommodationRoom;
import com.example.moodchon.domain.accommodation.external.TourApiLodgingIntroFields;
import com.example.moodchon.domain.accommodation.external.TourApiRoom;
import com.example.moodchon.domain.accommodation.repository.AccommodationVoteRepository;
import com.example.moodchon.domain.accommodation.repository.RecommendedAccommodationRepository;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import com.example.moodchon.domain.chonkangs.service.ChonkangsAccessValidator;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.mood.entity.MoodTag;
import com.example.moodchon.domain.mood.repository.MoodTypeRepository;
import com.example.moodchon.domain.place.repository.AccommodationCandidateRow;
import com.example.moodchon.domain.place.repository.PlaceRepository;
import com.example.moodchon.global.exception.CustomException;
import com.example.moodchon.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 추천 생성의 DB 작업 단위. 배치마다 트랜잭션을 끊어서 저장하므로, 전부 끝나기 전에도
// 지금까지 나온 추천을 화면에서 볼 수 있다.
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendedAccommodationGenerationWorker {

    private static final int TEXT_COLUMN_LIMIT = 255;

    private final ChonkangsAccessValidator chonkangsAccessValidator;
    private final ChonkangsRepository chonkangsRepository;
    private final PlaceRepository placeRepository;
    private final MoodTypeRepository moodTypeRepository;
    private final RecommendedAccommodationRepository recommendedAccommodationRepository;
    private final AccommodationVoteRepository accommodationVoteRepository;

    // 권한·무드 확정 여부를 확인하고 기존 추천을 비운 뒤, 이후 단계에 필요한 값만 복사해 돌려준다.
    @Transactional
    public ChonkangMoodContext prepare(Long chonkangId, Long userId) {
        chonkangsAccessValidator.validateMember(chonkangId, userId);

        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (!chonkang.isMoodDecided()) {
            throw new CustomException(ErrorCode.MOOD_NOT_DECIDED);
        }

        List<Long> existingIds = recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId)
                .stream()
                .map(RecommendedAccommodation::getId)
                .toList();
        accommodationVoteRepository.deleteAllByRecommendedAccommodationIdIn(existingIds);
        recommendedAccommodationRepository.deleteAllByChonkangId(chonkangId);
        recommendedAccommodationRepository.flush();

        return new ChonkangMoodContext(
                chonkang.getMoodName(),
                chonkang.getMoodDescription(),
                chonkang.getPlannedMemberCount(),
                chonkang.getDesiredRegion(),
                Set.copyOf(chonkang.getAccommodationConditions()));
    }

    // 무드의 핵심 태그와 많이 겹치는 순으로 후보를 고른다. 태그는 숙소를 수집할 때 이미 붙어 있어
    // 조회 한 번으로 끝난다. 태그 겹침은 거르는 기준이 아니라 줄 세우는 기준으로만 쓴다 -
    // 실측해 보니 태그가 하나도 안 겹쳐도 AI 매칭률이 90인 숙소가 있었다.
    @Transactional(readOnly = true)
    public List<Place> findCandidates(String moodName, String addressPrefix, int limit) {
        List<Long> coreTagIds = moodTypeRepository.findAllWithCoreTags().stream()
                .filter(moodType -> moodType.getName().equals(moodName))
                .findFirst()
                .map(moodType -> moodType.getCoreTags().stream().map(MoodTag::getId).toList())
                .orElse(List.of());

        // 네이티브 쿼리의 IN 절이 빈 목록을 허용하지 않아 매칭되지 않는 값을 넣는다.
        List<Long> safeTagIds = coreTagIds.isEmpty() ? List.of(-1L) : coreTagIds;

        List<Long> placeIds = placeRepository
                .findAccommodationCandidates(safeTagIds, addressPrefix, limit)
                .stream()
                .map(AccommodationCandidateRow::getPlaceId)
                .toList();

        // findAllById는 순서를 보장하지 않아 조회 순서(태그 겹침 순)대로 다시 정렬한다.
        Map<Long, Place> byId = placeRepository.findAllById(placeIds).stream()
                .collect(java.util.stream.Collectors.toMap(Place::getId, place -> place));
        return placeIds.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
    }

    // 배치 하나를 저장하고, 지금까지 저장된 전체를 매칭 점수 순으로 다시 줄 세운다.
    // 배치마다 순위를 새로 매겨야 중간에 화면을 봐도 순위가 어긋나지 않는다.
    @Transactional
    public int saveBatch(Long chonkangId, List<AccommodationCandidate> batch, AccommodationMatchResult result,
                          Map<String, List<TourApiRoom>> roomsByContentId) {
        Chonkangs chonkang = chonkangsRepository.findById(chonkangId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Map<Long, AccommodationCandidate> candidateByPlaceId = new java.util.HashMap<>();
        for (AccommodationCandidate candidate : batch) {
            candidateByPlaceId.put(candidate.place().getId(), candidate);
        }

        Set<Long> saved = new HashSet<>();
        List<RecommendedAccommodation> entities = new ArrayList<>();
        int unknownId = 0;
        int duplicated = 0;

        for (AccommodationMatchResult.MatchedAccommodation matched : result.accommodations()) {
            AccommodationCandidate candidate = candidateByPlaceId.get(matched.placeId());
            // AI가 후보에 없는 id를 만들어내거나 같은 숙소를 두 번 답하는 경우가 있어 걸러낸다.
            if (candidate == null) {
                unknownId++;
                continue;
            }
            if (!saved.add(matched.placeId())) {
                duplicated++;
                continue;
            }

            Place place = placeRepository.findById(matched.placeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
            TourApiLodgingIntroFields intro = candidate.lodgingIntro();

            RecommendedAccommodation entity = RecommendedAccommodation.builder()
                    .chonkang(chonkang)
                    .place(place)
                    .matchScore(clampScore(matched.matchScore()))
                    // 저장 직후 전체를 다시 줄 세우므로 여기서는 임시값을 넣는다.
                    .rank(0)
                    .tags(matched.tags())
                    .highlights(matched.highlights())
                    .regrets(matched.regrets())
                    .barbecueAvailable(parseFlag(intro.barbecue()))
                    .cookingAvailable(parseAvailability(intro.chkCooking()))
                    .petFriendly(parsePetFriendly(intro.petAccompanyType()))
                    .bicycleAvailable(parseFlag(intro.bicycle()))
                    .campfireAvailable(parseFlag(intro.campfire()))
                    .parkingAvailable(parseAvailability(intro.parkingLodging()))
                    .saunaAvailable(parseFlag(intro.sauna()))
                    .sportsAvailable(parseFlag(intro.sports()))
                    .checkInTime(limit("checkInTime", intro.checkInTime()))
                    .checkOutTime(limit("checkOutTime", intro.checkOutTime()))
                    .contact(limit("contact", intro.infoCenterLodging()))
                    .reservationUrl(limit("reservationUrl", intro.reservationUrl()))
                    .build();

            entity.replaceRooms(toRooms(entity, roomsByContentId.getOrDefault(
                    place.getExternalContentId(), List.of())));
            entities.add(entity);
        }

        // 60개를 다 못 채우는 원인을 구분해서 남긴다.
        log.info("[숙소 추천] 배치 결과 | 후보={} AI응답={} 저장={} 미응답={} 없는id={} 중복={}",
                batch.size(), result.accommodations().size(), entities.size(),
                batch.size() - result.accommodations().size(), unknownId, duplicated);

        recommendedAccommodationRepository.saveAll(entities);
        rerankAll(chonkangId);
        return entities.size();
    }

    // 배치가 쌓일 때마다 전체를 매칭 점수 내림차순으로 다시 매긴다. 60건 규모라 부담이 없다.
    private void rerankAll(Long chonkangId) {
        List<RecommendedAccommodation> all =
                recommendedAccommodationRepository.findAllByChonkangIdOrderByRankAsc(chonkangId);

        List<RecommendedAccommodation> sorted = new ArrayList<>(all);
        sorted.sort(Comparator.comparingInt(RecommendedAccommodation::getMatchScore).reversed());

        int rank = 1;
        for (RecommendedAccommodation entity : sorted) {
            entity.updateRank(rank++);
        }
    }

    private List<RecommendedAccommodationRoom> toRooms(RecommendedAccommodation entity, List<TourApiRoom> rooms) {
        return rooms.stream()
                .filter(room -> room.title() != null)
                .map(room -> RecommendedAccommodationRoom.builder()
                        .recommendedAccommodation(entity)
                        .name(room.title())
                        .imageUrl(room.imageUrl())
                        .roomCount(room.roomCount())
                        .baseCount(room.baseCount())
                        .maxCount(room.maxCount())
                        .offSeasonWeekdayFee(room.offSeasonWeekdayFee())
                        .offSeasonWeekendFee(room.offSeasonWeekendFee())
                        .peakSeasonWeekdayFee(room.peakSeasonWeekdayFee())
                        .peakSeasonWeekendFee(room.peakSeasonWeekendFee())
                        .facilities(room.facilities())
                        .build())
                .toList();
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    // barbecue는 TourAPI가 "1"/"0"으로 내려주는 플래그 필드다.
    private Boolean parseFlag(String rawValue) {
        if ("1".equals(rawValue)) {
            return true;
        }
        if ("0".equals(rawValue)) {
            return false;
        }
        return null;
    }

    // chkCooking은 "가능"/"불가능"/빈값처럼 자유 텍스트로 내려온다.
    private Boolean parseAvailability(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue.contains("불가능")) {
            return false;
        }
        if (rawValue.contains("가능")) {
            return true;
        }
        return null;
    }

    // detailPetTour2는 등록된 숙소만 결과를 주기 때문에 값이 있으면 동반 가능으로 보고,
    // 없으면 "명시적으로 불가능"이 아니라 "정보없음"으로 다룬다.
    private Boolean parsePetFriendly(String petAccompanyType) {
        return (petAccompanyType == null || petAccompanyType.isBlank()) ? null : true;
    }

    // TourAPI 자유 입력 필드라 드물게 컬럼 길이를 넘는 값이 온다. 값 하나 때문에 추천 생성 전체가
    // 실패하지 않도록 잘라서 저장하고, 어떤 필드가 길었는지 로그로 남긴다.
    private String limit(String fieldName, String rawValue) {
        String value = (rawValue == null || rawValue.isBlank()) ? null : rawValue.trim();
        if (value == null || value.length() <= TEXT_COLUMN_LIMIT) {
            return value;
        }
        log.warn("[숙소 추천] {} 값이 {}자라 {}자로 잘랐다 | 원문={}",
                fieldName, value.length(), TEXT_COLUMN_LIMIT, value);
        return value.substring(0, TEXT_COLUMN_LIMIT);
    }
}
