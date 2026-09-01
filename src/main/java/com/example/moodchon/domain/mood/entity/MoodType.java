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
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// "태그 체계 및 사용자 무드 유형" 문서의 공통 무드 유형(7개) 카탈로그. 시드는 MoodTagSeeder가 앱 기동 시 채움.
@Entity
@Table(name = "mood_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MoodType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "mood_type_core_tags",
            joinColumns = @JoinColumn(name = "mood_type_id"),
            inverseJoinColumns = @JoinColumn(name = "mood_tag_id")
    )
    private Set<MoodTag> coreTags = new HashSet<>();

    @Builder
    private MoodType(String name, String description, Set<MoodTag> coreTags) {
        this.name = name;
        this.description = description;
        this.coreTags = coreTags != null ? coreTags : new HashSet<>();
    }
}
