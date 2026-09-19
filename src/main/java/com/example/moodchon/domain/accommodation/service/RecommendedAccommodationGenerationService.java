package com.example.moodchon.domain.accommodation.service;

import com.example.moodchon.domain.accommodation.ai.AccommodationCandidate;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchContext;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatchResult;
import com.example.moodchon.domain.accommodation.ai.AccommodationMatcher;
import com.example.moodchon.domain.accommodation.external.TourApiClient;
import com.example.moodchon.domain.accommodation.external.TourApiLodgingIntroFields;
import com.example.moodchon.domain.accommodation.external.TourApiRoom;
import com.example.moodchon.domain.place.entity.Place;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// 숙소 추천 생성의 오케스트레이션. 클래스 레벨 @Transactional을 걸지 않는다.
// 배치마다 따로 커밋해야 전부 끝나기 전에도 지금까지 나온 추천이 화면에 보인다.
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendedAccommodationGenerationService {

    private static final int CANDIDATE_COUNT = 60;
    // AI에게 한 번에 너무 많이 물으면 일부 숙소를 빠뜨린 채 답한다. 나눠서 여러 번 호출한다.
    private static final int BATCH_SIZE = 20;
    private static final int TOUR_API_CONCURRENCY = 8;

    // 후보 1곳당 TourAPI를 3번(편의시설/반려동물/객실) 부른다. 순차로는 너무 느리다.
    private final ExecutorService tourApiExecutor = Executors.newFixedThreadPool(TOUR_API_CONCURRENCY);

    private final TourApiClient tourApiClient;
    private final AccommodationMatcher accommodationMatcher;
    private final RecommendedAccommodationGenerationWorker worker;

    public void generate(Long chonkangId, Long userId) {
        ChonkangMoodContext mood = worker.prepare(chonkangId, userId);

        // 이미 수집·태깅된 숙소 중에서 고른다. TourAPI로 새 숙소를 끌어오면 개요·사진 수집과
        // 무드 태깅까지 하느라 후보 60곳에 80초 넘게 걸렸다. 그 비용을 통째로 없앤다.
        List<Place> candidates = worker.findCandidates(
                mood.moodName(), RegionAddressPrefix.resolve(mood.desiredRegion()), CANDIDATE_COUNT);

        log.info("[숙소 추천] 후보 {}곳 선정 | region={} chonkangId={}",
                candidates.size(), mood.desiredRegion(), chonkangId);

        if (candidates.isEmpty()) {
            return;
        }

        // 외부 호출만 병렬로 돌리고, JPA 엔티티를 만지는 일은 워커의 트랜잭션 안에서 처리한다.
        List<TourApiLodgingIntroFields> intros = fetchInParallel(
                candidates.stream().map(Place::getExternalContentId).toList(),
                tourApiClient::fetchLodgingIntro);

        List<AccommodationCandidate> matchCandidates = IntStream.range(0, candidates.size())
                .mapToObj(i -> new AccommodationCandidate(candidates.get(i), intros.get(i)))
                .toList();

        int savedTotal = 0;
        for (int from = 0; from < matchCandidates.size(); from += BATCH_SIZE) {
            int to = Math.min(from + BATCH_SIZE, matchCandidates.size());
            List<AccommodationCandidate> batch = matchCandidates.subList(from, to);

            savedTotal += processBatch(chonkangId, mood, batch);
            log.info("[숙소 추천] {}/{}곳 평가 완료, 누적 저장 {}건 | chonkangId={}",
                    to, matchCandidates.size(), savedTotal, chonkangId);
        }
    }

    // 배치 하나를 AI로 평가하고 저장한다. 한 배치가 실패해도 나머지 배치는 계속 진행한다.
    private int processBatch(Long chonkangId, ChonkangMoodContext mood, List<AccommodationCandidate> batch) {
        try {
            AccommodationMatchResult result = accommodationMatcher.match(new AccommodationMatchContext(
                    mood.moodName(), mood.moodDescription(), mood.plannedMemberCount(),
                    mood.accommodationConditions(), batch));

            Map<String, List<TourApiRoom>> rooms = fetchRooms(batch);
            return worker.saveBatch(chonkangId, batch, result, rooms);
        } catch (RuntimeException e) {
            log.warn("[숙소 추천] 배치 처리 실패, 다음 배치로 진행 | chonkangId={} error={}", chonkangId, e.toString());
            return 0;
        }
    }

    private Map<String, List<TourApiRoom>> fetchRooms(List<AccommodationCandidate> batch) {
        List<String> contentIds = batch.stream()
                .map(candidate -> candidate.place().getExternalContentId())
                .toList();
        List<List<TourApiRoom>> fetched = fetchInParallel(contentIds, this::fetchRoomsOrEmpty);

        Map<String, List<TourApiRoom>> byContentId = new HashMap<>();
        for (int i = 0; i < contentIds.size(); i++) {
            byContentId.put(contentIds.get(i), fetched.get(i));
        }
        return byContentId;
    }

    // 객실 정보는 화면 표시용 부가 데이터라, 한 숙소가 실패해도 추천 생성을 중단시키지 않는다.
    private List<TourApiRoom> fetchRoomsOrEmpty(String externalContentId) {
        try {
            return tourApiClient.fetchRooms(externalContentId);
        } catch (RuntimeException e) {
            log.warn("[숙소 추천] 객실 조회 실패 | contentId={} error={}", externalContentId, e.toString());
            return List.of();
        }
    }

    // TourAPI 호출만 제한된 개수로 병렬 처리한다. 공공데이터포털이라 동시 호출을 과하게 늘리지 않는다.
    private <T, R> List<R> fetchInParallel(List<T> inputs, Function<T, R> fetcher) {
        List<CompletableFuture<R>> futures = inputs.stream()
                .map(input -> CompletableFuture.supplyAsync(() -> fetcher.apply(input), tourApiExecutor))
                .toList();

        try {
            return futures.stream().map(CompletableFuture::join).toList();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw e;
        }
    }
}
