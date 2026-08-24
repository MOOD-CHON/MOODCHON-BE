package com.example.moodchon.domain.chonkangs.entity;

import com.example.moodchon.domain.mood.entity.MoodCard;
import com.example.moodchon.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chonkang_mood_selections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chonkang_id", "user_id", "mood_card_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChonkangsMoodSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chonkang_id", nullable = false)
    private Chonkangs chonkang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mood_card_id", nullable = false)
    private MoodCard moodCard;

    @Builder
    private ChonkangsMoodSelection(Chonkangs chonkang, User user, MoodCard moodCard) {
        this.chonkang = chonkang;
        this.user = user;
        this.moodCard = moodCard;
    }
}
