package com.example.moodchon.domain.recommendation.entity;

import com.example.moodchon.domain.place.entity.Place;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private int dayNumber;

    @Column(nullable = false)
    private int orderInDay;

    @Column(nullable = false)
    private int moodFitScore;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportMode transportMode;

    @Column(nullable = false)
    private int travelMinutes;

    @Builder
    private RecommendedItineraryItem(RecommendedItinerary recommendedItinerary, Place place, int dayNumber,
                                      int orderInDay, int moodFitScore, String aiSummary,
                                      TransportMode transportMode, int travelMinutes) {
        this.recommendedItinerary = recommendedItinerary;
        this.place = place;
        this.dayNumber = dayNumber;
        this.orderInDay = orderInDay;
        this.moodFitScore = moodFitScore;
        this.aiSummary = aiSummary;
        this.transportMode = transportMode;
        this.travelMinutes = travelMinutes;
    }
}
