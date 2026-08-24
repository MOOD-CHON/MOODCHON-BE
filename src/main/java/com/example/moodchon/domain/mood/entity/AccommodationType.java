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

// 숙소 유형(펜션/글램핑/한옥 등) 카탈로그. 시드 데이터 별도 입력 필요.
@Entity
@Table(name = "accommodation_types")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccommodationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Builder
    private AccommodationType(String name) {
        this.name = name;
    }
}
