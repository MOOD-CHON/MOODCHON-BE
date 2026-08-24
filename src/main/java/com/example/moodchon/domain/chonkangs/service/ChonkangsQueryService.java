package com.example.moodchon.domain.chonkangs.service;

import com.example.moodchon.domain.chonkangs.dto.ChonkangsFilter;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangHomeResponse;
import com.example.moodchon.domain.chonkangs.dto.response.ChonkangSummaryResponse;
import com.example.moodchon.domain.chonkangs.dto.response.UpcomingChonkangResponse;
import com.example.moodchon.domain.chonkangs.entity.Chonkangs;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsMemberRepository;
import com.example.moodchon.domain.chonkangs.repository.ChonkangsRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChonkangsQueryService {

    private final ChonkangsRepository chonkangRepository;
    private final ChonkangsMemberRepository chonkangMemberRepository;

    public ChonkangHomeResponse getHome(Long userId) {
        LocalDate today = LocalDate.now();
        List<Chonkangs> chonkangs = chonkangRepository.findAllByMemberUserId(userId);

        UpcomingChonkangResponse upcoming = chonkangs.stream()
                .filter(chonkang -> !chonkang.isCompleted(today))
                .min(Comparator.comparing(Chonkangs::getStartDate))
                .map(chonkang -> UpcomingChonkangResponse.of(chonkang, today, memberCount(chonkang)))
                .orElse(null);

        List<ChonkangSummaryResponse> records = toSummaries(chonkangs, today);

        return ChonkangHomeResponse.of(upcoming, records);
    }

    public List<ChonkangSummaryResponse> getMyChonkangs(Long userId, ChonkangsFilter filter) {
        LocalDate today = LocalDate.now();
        List<Chonkangs> chonkangs = chonkangRepository.findAllByMemberUserId(userId);

        return toSummaries(chonkangs, today).stream()
                .filter(summary -> filter.matches(summary.status()))
                .toList();
    }

    private List<ChonkangSummaryResponse> toSummaries(List<Chonkangs> chonkangs, LocalDate today) {
        return chonkangs.stream()
                .map(chonkang -> ChonkangSummaryResponse.of(chonkang, today, memberCount(chonkang)))
                .toList();
    }

    private long memberCount(Chonkangs chonkang) {
        return chonkangMemberRepository.countByChonkangId(chonkang.getId());
    }
}
