package com.example.moodchon.domain.mood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// "태그 체계 및 사용자 무드 유형" 문서의 무드 도출용 태그(22개) 카탈로그. 시드는 MoodTagSeeder가 앱 기동 시 채움.
@Entity
@Table(name = "mood_tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoodTagCategory category;

    @Builder
    private MoodTag(String name, MoodTagCategory category) {
        this.name = name;
        this.category = category;
    }
}
