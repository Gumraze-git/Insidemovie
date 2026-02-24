package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.match.entity.Match;
import com.insidemovie.backend.api.match.entity.MovieMatch;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.match.repository.VoteRepository;
import com.insidemovie.backend.api.match.service.MatchService;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchSeedService {

    private final MatchService matchService;
    private final MatchRepository matchRepository;
    private final MovieMatchRepository movieMatchRepository;
    private final VoteRepository voteRepository;
    private final MemberRepository memberRepository;
    private final DemoAccountCatalogService demoAccountCatalogService;

    @Transactional
    public MatchSeedReport seed(boolean dryRun, int closedTargetCount, int currentVoteTarget) {
        int closedMatchesCreated = 0;
        int currentMatchesCreated = 0;
        int votesCreated = 0;

        List<Long> voterIds = resolveGeneralMemberIds();
        if (voterIds.isEmpty()) {
            return MatchSeedReport.builder()
                    .closedMatchesCreated(0)
                    .currentMatchesCreated(0)
                    .votesCreated(0)
                    .build();
        }

        long closed = matchRepository.countByWinnerIdIsNotNull();
        if (dryRun) {
            int missingClosed = Math.max(0, closedTargetCount - (int) closed);
            int currentNeeded = matchRepository.countByWinnerIdIsNull() == 0 ? 1 : 0;
            int plannedVotes = (missingClosed * voterIds.size()) + (currentNeeded > 0 ? Math.min(currentVoteTarget, voterIds.size()) : 0);
            return MatchSeedReport.builder()
                    .closedMatchesCreated(missingClosed)
                    .currentMatchesCreated(currentNeeded)
                    .votesCreated(plannedVotes)
                    .build();
        }

        while (matchRepository.countByWinnerIdIsNotNull() < closedTargetCount) {
            Optional<Match> openMatch = matchRepository.findTopByWinnerIdIsNullOrderByMatchNumberDesc();
            if (openMatch.isEmpty()) {
                matchService.createMatch();
            }

            votesCreated += seedVotesForLatestOpenMatch(voterIds, voterIds.size());
            matchService.closeMatch();
            closedMatchesCreated++;
        }

        long openCount = matchRepository.countByWinnerIdIsNull();
        if (openCount == 0) {
            matchService.createMatch();
            currentMatchesCreated++;
        } else if (openCount > 1) {
            log.warn("[MatchSeed] found {} open matches, keeping latest open without destructive cleanup", openCount);
        }

        votesCreated += seedVotesForLatestOpenMatch(voterIds, currentVoteTarget);

        return MatchSeedReport.builder()
                .closedMatchesCreated(closedMatchesCreated)
                .currentMatchesCreated(currentMatchesCreated)
                .votesCreated(votesCreated)
                .build();
    }

    private int seedVotesForLatestOpenMatch(List<Long> voterIds, int maxVoters) {
        Optional<Match> openMatchOptional = matchRepository.findTopByWinnerIdIsNullOrderByMatchNumberDesc();
        if (openMatchOptional.isEmpty()) {
            return 0;
        }

        Match match = openMatchOptional.get();
        List<MovieMatch> choices = movieMatchRepository.findByMatchId(match.getId());
        if (choices.isEmpty()) {
            return 0;
        }

        Random random = new Random(match.getId());
        List<Long> shuffled = new ArrayList<>(voterIds);
        Collections.shuffle(shuffled, random);
        int limit = Math.min(maxVoters, shuffled.size());
        int created = 0;

        for (int i = 0; i < limit; i++) {
            Long userId = shuffled.get(i);
            if (voteRepository.existsByMatchIdAndMemberId(match.getId(), userId)) {
                continue;
            }

            MovieMatch selected = choices.get(random.nextInt(choices.size()));
            try {
                matchService.voteMatch(selected.getMovie().getId(), userId);
                created++;
            } catch (Exception e) {
                log.warn("[MatchSeed] vote failed matchId={} userId={} movieId={} error={}",
                        match.getId(), userId, selected.getMovie().getId(), e.getMessage());
            }
        }
        return created;
    }

    private List<Long> resolveGeneralMemberIds() {
        List<DemoAccountCatalogService.DemoAccountDefinition> generalDefinitions =
                demoAccountCatalogService.getGeneralAccountDefinitions();

        return generalDefinitions.stream()
                .map(def -> memberRepository.findByEmail(def.email()))
                .flatMap(Optional::stream)
                .filter(member -> !member.isBanned())
                .map(Member::getId)
                .toList();
    }
}

