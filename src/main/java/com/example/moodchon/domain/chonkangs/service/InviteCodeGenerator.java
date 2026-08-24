package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InviteCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ChonkangsRepository chonkangsRepository;

    public String generate() {
        String code;
        do {
            code = randomCode();
        } while (chonkangsRepository.existsByInviteCode(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
