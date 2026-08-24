package com.example.moodchon.domain.recommendation.entity;

import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recommended_itineraries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendedItinerary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chonkang_id", nullable = false, unique = true)
    private Chonkangs chonkang;

    @Column(nullable = false)
    private boolean committed;

    private LocalDateTime committedAt;

    @Builder
    private RecommendedItinerary(Chonkangs chonkang) {
        this.chonkang = chonkang;
        this.committed = false;
    }

    public void commit() {
        this.committed = true;
        this.committedAt = LocalDateTime.now();
    }
}
