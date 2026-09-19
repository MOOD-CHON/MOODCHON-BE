package com.example.moodchon.domain.chonkangs.service;

// 무드가 막 확정된 방의 숙소 추천을 트랜잭션 커밋 후 비동기로 생성하기 위한 이벤트.
// userId는 그 시점에 마지막으로 무드를 제출한 사람 - 숙소 추천 생성 시 멤버 검증에 쓴다.
public record MoodConfirmedEvent(Long chonkangId, Long userId) {
}
