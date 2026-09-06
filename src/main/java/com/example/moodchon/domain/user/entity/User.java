package com.example.moodchon.domain.user.entity;

import com.example.moodchon.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE users SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    private static final String WITHDRAWN_PROVIDER_ID_PREFIX = "withdrawn_";
    private static final String WITHDRAWN_NICKNAME = "탈퇴한 사용자";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private boolean notificationEnabled = true;

    @Builder
    private User(AuthProvider provider, String providerId, String nickname, String profileImageUrl, UserRole role) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role;
        this.notificationEnabled = true;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    // (provider, provider_id) 유니크 제약은 소프트 딜리트된 행에도 그대로 걸려 있어,
    // providerId를 그대로 두면 같은 소셜 계정으로 재가입할 때 제약 위반이 난다.
    // 탈퇴 시점에 연결을 끊어 재가입이 새 계정으로 이뤄지게 하고 소셜 식별자도 남기지 않는다.
    // 행 자체는 촌캉스 host/멤버 참조 무결성 때문에 남기되, 닉네임/프로필 이미지 등
    // 개인을 식별할 수 있는 값은 모두 지워서 탈퇴 의사대로 완전히 사라진 것처럼 처리한다.
    public void withdraw() {
        this.providerId = WITHDRAWN_PROVIDER_ID_PREFIX + this.id + "_" + this.providerId;
        this.nickname = WITHDRAWN_NICKNAME;
        this.profileImageUrl = null;
        delete();
    }
}