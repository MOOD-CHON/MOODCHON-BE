package com.example.moodchon.domain.accommodation.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// TourAPI detailInfo2에서 가져온 객실 정보. 숙소 추천을 만들 때 함께 저장해두고
// 상세 화면에서 그대로 보여준다. 등록이 안 된 숙소는 객실이 0개다.
@Entity
@Table(name = "recommended_accommodation_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedAccommodationRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_accommodation_id", nullable = false)
    private RecommendedAccommodation recommendedAccommodation;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    // TourAPI에 등록 안 된 항목은 null이고, 화면에서 해당 줄을 숨긴다.
    private Integer roomCount;
    private Integer baseCount;
    private Integer maxCount;

    // 원 단위 최소 요금.
    private Integer offSeasonWeekdayFee;
    private Integer offSeasonWeekendFee;
    private Integer peakSeasonWeekdayFee;
    private Integer peakSeasonWeekendFee;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommended_accommodation_room_facilities",
            joinColumns = @JoinColumn(name = "recommended_accommodation_room_id"))
    @OrderColumn(name = "facility_order")
    @Column(name = "facility", nullable = false)
    private List<String> facilities = new ArrayList<>();

    @Builder
    private RecommendedAccommodationRoom(RecommendedAccommodation recommendedAccommodation, String name,
                                          String imageUrl, Integer roomCount, Integer baseCount, Integer maxCount,
                                          Integer offSeasonWeekdayFee, Integer offSeasonWeekendFee,
                                          Integer peakSeasonWeekdayFee, Integer peakSeasonWeekendFee,
                                          List<String> facilities) {
        this.recommendedAccommodation = recommendedAccommodation;
        this.name = name;
        this.imageUrl = imageUrl;
        this.roomCount = roomCount;
        this.baseCount = baseCount;
        this.maxCount = maxCount;
        this.offSeasonWeekdayFee = offSeasonWeekdayFee;
        this.offSeasonWeekendFee = offSeasonWeekendFee;
        this.peakSeasonWeekdayFee = peakSeasonWeekdayFee;
        this.peakSeasonWeekendFee = peakSeasonWeekendFee;
        this.facilities = facilities != null ? facilities : new ArrayList<>();
    }
}
