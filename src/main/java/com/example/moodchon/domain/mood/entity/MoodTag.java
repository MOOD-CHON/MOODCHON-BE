package com.example.moodchon.domain.mood.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// "태그 체계 및 사용자 무드 유형" 문서의 무드 도출용 태그(22개) 카탈로그. 시드 데이터 별도 입력 필요.
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

    @Builder
    private MoodTag(String name) {
        this.name = name;
    }
}
