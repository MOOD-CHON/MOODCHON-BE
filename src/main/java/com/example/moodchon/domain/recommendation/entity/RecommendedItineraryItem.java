package com.example.moodchon.domain.recommendation.entity;

import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.domain.place.entity.PlaceCategory;
import com.example.moodchon.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommended_itinerary_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedItineraryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_itinerary_id", nullable = false)
    private RecommendedItinerary recommendedItinerary;

    // AI가 생성했거나 사용자가 검색해서 추가한 경우에만 채워짐. "장소 없이 추가하기"로 만든 항목은 null이고
    // 대신 customName/customCategory로 표시한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    private String customName;

    @Enumerated(EnumType.STRING)
    private PlaceCategory customCategory;

    @Column(nullable = false)
    private int dayNumber;

    @Column(nullable = false)
    private int orderInDay;

    // 장소 없이 추가한 항목은 무드 매칭 대상이 아니라 0으로 고정한다.
    @Column(nullable = false)
    private int moodFitScore;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    private TransportMode transportMode;

    private Integer travelMinutes;

    @Builder
    private RecommendedItineraryItem(RecommendedItinerary recommendedItinerary, Place place, String customName,
                                      PlaceCategory customCategory, int dayNumber, int orderInDay, int moodFitScore,
                                      String aiSummary, TransportMode transportMode, Integer travelMinutes) {
        this.recommendedItinerary = recommendedItinerary;
        this.place = place;
        this.customName = customName;
        this.customCategory = customCategory;
        this.dayNumber = dayNumber;
        this.orderInDay = orderInDay;
        this.moodFitScore = moodFitScore;
        this.aiSummary = aiSummary;
        this.transportMode = transportMode;
        this.travelMinutes = travelMinutes;
    }

    public void changeOrder(int orderInDay) {
        this.orderInDay = orderInDay;
    }

    public boolean isCustom() {
        return place == null;
    }
}
