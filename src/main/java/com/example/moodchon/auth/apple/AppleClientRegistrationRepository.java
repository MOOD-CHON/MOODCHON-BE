package com.example.moodchon.auth.apple;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * 애플 등록 정보를 조회할 때마다 client_secret을 새로 생성된 JWT로 갈아끼운다.
 * (그 외 provider는 위임 저장소의 등록 정보를 그대로 반환)
 */
public class AppleClientRegistrationRepository implements ClientRegistrationRepository {

    private static final String APPLE_REGISTRATION_ID = "apple";

    private final ClientRegistrationRepository delegate;
    private final AppleClientSecretGenerator appleClientSecretGenerator;

    public AppleClientRegistrationRepository(ClientRegistrationRepository delegate,
                                              AppleClientSecretGenerator appleClientSecretGenerator) {
        this.delegate = delegate;
        this.appleClientSecretGenerator = appleClientSecretGenerator;
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        ClientRegistration registration = delegate.findByRegistrationId(registrationId);
        if (registration == null || !APPLE_REGISTRATION_ID.equals(registrationId)) {
            return registration;
        }

        return ClientRegistration.withClientRegistration(registration)
                .clientSecret(appleClientSecretGenerator.generate())
                .build();
    }
}