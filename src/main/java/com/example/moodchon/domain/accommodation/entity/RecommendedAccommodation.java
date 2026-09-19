package com.example.moodchon.domain.accommodation.entity;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.place.entity.Place;
import com.example.moodchon.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
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

    // AI가 만든 "아쉬운 점". highlights(좋은 점)와 대칭되는 목록.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recommended_accommodation_regrets",
            joinColumns = @JoinColumn(name = "recommended_accommodation_id"))
    @OrderColumn(name = "regret_order")
    @Column(name = "regret", columnDefinition = "TEXT", nullable = false)
    private List<String> regrets = new ArrayList<>();

    // TourAPI 원본 값을 매칭 시점에 그대로 저장한 편의시설 정보. null은 "정보없음"을 뜻하고
    // 감점 근거로 쓰지 않는 것과 동일하게, 화면에서도 뱃지를 표시하지 않는 상태로 다뤄야 한다.
    private Boolean barbecueAvailable;
    private Boolean cookingAvailable;
    private Boolean petFriendly;
    private Boolean bicycleAvailable;
    private Boolean campfireAvailable;
    private Boolean parkingAvailable;
    private Boolean saunaAvailable;
    private Boolean sportsAvailable;

    private String checkInTime;
    private String checkOutTime;
    private String contact;
    private String reservationUrl;

    // TourAPI detailInfo2에서 가져온 객실 목록. 추천을 지우면 객실도 같이 지운다.
    @OneToMany(mappedBy = "recommendedAccommodation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "room_order")
    private List<RecommendedAccommodationRoom> rooms = new ArrayList<>();

    @Builder
    private RecommendedAccommodation(Chonkangs chonkang, Place place, int matchScore, int rank,
                                      List<String> tags, List<String> highlights, List<String> regrets,
                                      Boolean barbecueAvailable, Boolean cookingAvailable, Boolean petFriendly,
                                      Boolean bicycleAvailable, Boolean campfireAvailable, Boolean parkingAvailable,
                                      Boolean saunaAvailable, Boolean sportsAvailable,
                                      String checkInTime, String checkOutTime, String contact,
                                      String reservationUrl) {
        this.chonkang = chonkang;
        this.place = place;
        this.matchScore = matchScore;
        this.rank = rank;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.highlights = highlights != null ? highlights : new ArrayList<>();
        this.regrets = regrets != null ? regrets : new ArrayList<>();
        this.barbecueAvailable = barbecueAvailable;
        this.cookingAvailable = cookingAvailable;
        this.petFriendly = petFriendly;
        this.bicycleAvailable = bicycleAvailable;
        this.campfireAvailable = campfireAvailable;
        this.parkingAvailable = parkingAvailable;
        this.saunaAvailable = saunaAvailable;
        this.sportsAvailable = sportsAvailable;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.contact = contact;
        this.reservationUrl = reservationUrl;
    }

    // 배치로 나눠 저장하기 때문에 새 배치가 들어올 때마다 전체 순위를 다시 매긴다.
    public void updateRank(int rank) {
        this.rank = rank;
    }

    // 객실은 추천을 저장한 뒤 채운다. 양쪽 참조를 함께 맞춰야 cascade 저장이 동작한다.
    public void replaceRooms(List<RecommendedAccommodationRoom> newRooms) {
        this.rooms.clear();
        if (newRooms != null) {
            this.rooms.addAll(newRooms);
        }
    }
}
