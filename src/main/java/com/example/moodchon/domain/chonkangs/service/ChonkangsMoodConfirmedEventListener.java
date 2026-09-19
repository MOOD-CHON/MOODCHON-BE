package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.accommodation.service.RecommendedAccommodationGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// 무드 확정 트랜잭션이 실제로 커밋된 뒤에만(AFTER_COMMIT) 별도 스레드(@Async)에서 숙소 추천을
// 생성한다. 커밋 전에 시작하면 아직 안 보이는 방/무드 선택을 조회하려다 실패할 수 있고,
// 원래 요청(방 생성 등)과 같은 스레드에서 동기로 하면 TourAPI/OpenAI 호출이 누적돼
// 수십 초가 걸려 클라이언트가 타임아웃난다.
@Slf4j
@Component
@RequiredArgsConstructor
public class ChonkangsMoodConfirmedEventListener {

    private final RecommendedAccommodationGenerationService recommendedAccommodationGenerationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMoodConfirmed(MoodConfirmedEvent event) {
        try {
            recommendedAccommodationGenerationService.generate(event.chonkangId(), event.userId());
        } catch (Exception e) {
            log.warn("숙소 추천 생성 실패: chonkangId={}", event.chonkangId(), e);
        }
    }
}
