package com.example.moodchon.domain.chonkangs.entity;

import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.global.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "chonkangs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE chonkangs SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Chonkangs extends BaseEntity {

    public static final int MAX_MEMBER_COUNT = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String moodName;

    @Column(columnDefinition = "TEXT")
    private String moodDescription;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int plannedMemberCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanionType companionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelMethod travelMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Region desiredRegion;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "chonkang_accommodation_conditions", joinColumns = @JoinColumn(name = "chonkang_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private Set<AccommodationCondition> accommodationConditions = new HashSet<>();

    private String thumbnailUrl;

    @Column(nullable = false, unique = true)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Builder
    private Chonkangs(String name, LocalDate startDate, LocalDate endDate, int plannedMemberCount,
                      CompanionType companionType, TravelMethod travelMethod, Region desiredRegion,
                      Set<AccommodationCondition> accommodationConditions, String inviteCode, User host) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.plannedMemberCount = plannedMemberCount;
        this.companionType = companionType;
        this.travelMethod = travelMethod;
        this.desiredRegion = desiredRegion;
        this.accommodationConditions = accommodationConditions != null ? accommodationConditions : new HashSet<>();
        this.inviteCode = inviteCode;
        this.host = host;
    }

    public boolean isCompleted(LocalDate today) {
        return endDate.isBefore(today);
    }

    public void confirmMood(String moodName, String moodDescription) {
        this.moodName = moodName;
        this.moodDescription = moodDescription;
    }

    public void resetMood() {
        this.moodName = null;
        this.moodDescription = null;
    }

    public boolean isMoodDecided() {
        return moodName != null;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateTripInfo(String name, LocalDate startDate, LocalDate endDate, int plannedMemberCount,
                                CompanionType companionType, TravelMethod travelMethod, Region desiredRegion,
                                Set<AccommodationCondition> accommodationConditions) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.plannedMemberCount = plannedMemberCount;
        this.companionType = companionType;
        this.travelMethod = travelMethod;
        this.desiredRegion = desiredRegion;
        this.accommodationConditions = accommodationConditions != null ? accommodationConditions : new HashSet<>();
    }

    public ChonkangsStatus resolveStatus(LocalDate today) {
        return isCompleted(today) ? ChonkangsStatus.COMPLETED : ChonkangsStatus.ONGOING;
    }

    public ChonkangsMainStatus resolveMainStatus() {
        return isMoodDecided() ? ChonkangsMainStatus.MOOD_DECIDED : ChonkangsMainStatus.MOOD_VOTING;
    }
}
