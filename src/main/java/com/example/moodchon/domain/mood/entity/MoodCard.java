package com.example.moodchon.domain.mood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mood_cards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_type_id", nullable = false)
    private AccommodationType accommodationType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mood_card_tags",
            joinColumns = @JoinColumn(name = "mood_card_id"),
            inverseJoinColumns = @JoinColumn(name = "mood_tag_id")
    )
    private Set<MoodTag> tags = new HashSet<>();

    @Builder
    private MoodCard(String imageUrl, AccommodationType accommodationType, Set<MoodTag> tags) {
        this.imageUrl = imageUrl;
        this.accommodationType = accommodationType;
        this.tags = tags != null ? tags : new HashSet<>();
    }
}
