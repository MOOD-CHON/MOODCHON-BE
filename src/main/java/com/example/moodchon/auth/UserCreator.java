package com.example.moodchon.auth;

import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// UserProvisioningService.saveOrUpdate()에서 동시 요청으로 인한 저장 실패를 별도 트랜잭션으로
// 격리하기 위한 헬퍼. REQUIRES_NEW가 같은 클래스 내부 호출(self-invocation)에서는 무시되므로
// 별도 빈으로 분리했다. PostgreSQL은 제약 위반이 나면 그 트랜잭션 전체가 abort되어 이후
// 쿼리가 모두 실패하기 때문에, 실패 시 재조회는 이 트랜잭션이 아닌 호출자의 트랜잭션에서 해야 한다.
@Component
@RequiredArgsConstructor
class UserCreator {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User create(OAuthAttributes attributes) {
        return userRepository.save(attributes.toEntity());
    }
}
