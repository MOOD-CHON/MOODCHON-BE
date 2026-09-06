package com.example.moodchon;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.moodchon.auth.OAuthAttributes;
import com.example.moodchon.auth.UserProvisioningService;
import com.example.moodchon.domain.user.entity.AuthProvider;
import com.example.moodchon.domain.user.entity.User;
import com.example.moodchon.domain.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MoodchonApplicationTests {

    @Autowired
    private UserProvisioningService userProvisioningService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void concurrentKakaoLoginDoesNotFail() throws Exception {
        String fakeProviderId = "race-test-" + System.currentTimeMillis();
        Map<String, Object> profile = Map.of("nickname", "레이스테스트", "profile_image_url", "http://x");
        Map<String, Object> kakaoAccount = Map.of("profile", profile);
        Map<String, Object> attributes = Map.of("id", fakeProviderId, "kakao_account", kakaoAccount);
        OAuthAttributes oAuthAttributes = OAuthAttributes.ofKakao(attributes);

        int concurrency = 5;
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<User>> futures = new java.util.ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();
                return userProvisioningService.saveOrUpdate(oAuthAttributes);
            }));
        }

        ready.await();
        start.countDown();

        List<Long> ids = new java.util.ArrayList<>();
        for (Future<User> f : futures) {
            ids.add(f.get().getId());
        }
        executor.shutdown();

        assertThat(ids).allMatch(id -> id.equals(ids.get(0)));
        assertThat(userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, fakeProviderId)).isPresent();

        userRepository.deleteById(ids.get(0));
    }

}
