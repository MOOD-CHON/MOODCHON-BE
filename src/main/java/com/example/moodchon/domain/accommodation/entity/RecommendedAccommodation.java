package com.example.moodchon.domain.accommodation.entity;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.global.entity.BaseEntity;
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

@Entity
@Table(name = "recommended_accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedAccommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chonkang_id", nullable = false)
    private Chonkangs chonkang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(nullable = false)
    private int matchScore;

    @Column(nullable = false)
    private int rank;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommended_accommodation_tags",
            joinColumns = @JoinColumn(name = "recommended_accommodation_id"))
    @OrderColumn(name = "tag_order")
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommended_accommodation_highlights",
            joinColumns = @JoinColumn(name = "recommended_accommodation_id"))
    @OrderColumn(name = "highlight_order")
    @Column(name = "highlight", columnDefinition = "TEXT", nullable = false)
    private List<String> highlights = new ArrayList<>();

    // TourAPI 원본 값을 매칭 시점에 그대로 저장한 편의시설 정보. null은 "정보없음"을 뜻하고
    // 감점 근거로 쓰지 않는 것과 동일하게, 화면에서도 뱃지를 표시하지 않는 상태로 다뤄야 한다.
    private Boolean barbecueAvailable;
    private Boolean cookingAvailable;
    private Boolean petFriendly;

    @Builder
    private RecommendedAccommodation(Chonkangs chonkang, Place place, int matchScore, int rank,
                                      List<String> tags, List<String> highlights,
                                      Boolean barbecueAvailable, Boolean cookingAvailable, Boolean petFriendly) {
        this.chonkang = chonkang;
        this.place = place;
        this.matchScore = matchScore;
        this.rank = rank;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.highlights = highlights != null ? highlights : new ArrayList<>();
        this.barbecueAvailable = barbecueAvailable;
        this.cookingAvailable = cookingAvailable;
        this.petFriendly = petFriendly;
    }
}
