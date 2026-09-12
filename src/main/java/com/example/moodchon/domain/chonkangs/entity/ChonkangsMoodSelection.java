package com.example.moodchon.domain.chonkangs.entity;

import com.example.moodchon.domain.place.entity.Post;
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
        uniqueConstraints = @UniqueConstraint(columnNames = {"chonkang_id", "user_id", "post_id"}))
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

    // 무드 카드로 쓰이는 탐색 탭 게시물. 무드는 이 게시물에 AI가 붙인 태그를 집계해서 결정한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Builder
    private ChonkangsMoodSelection(Chonkangs chonkang, User user, Post post) {
        this.chonkang = chonkang;
        this.user = user;
        this.post = post;
    }
}
