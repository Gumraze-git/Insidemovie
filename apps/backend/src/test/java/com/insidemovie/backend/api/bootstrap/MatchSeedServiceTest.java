package com.insidemovie.backend.api.bootstrap;

import com.insidemovie.backend.api.auth.service.DemoAccountCatalogService;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.match.repository.MatchRepository;
import com.insidemovie.backend.api.match.repository.MovieMatchRepository;
import com.insidemovie.backend.api.match.repository.VoteRepository;
import com.insidemovie.backend.api.match.service.MatchService;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchSeedServiceTest {

    @Mock
    private MatchService matchService;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MovieMatchRepository movieMatchRepository;
    @Mock
    private VoteRepository voteRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private DemoAccountCatalogService demoAccountCatalogService;

    @InjectMocks
    private MatchSeedService matchSeedService;

    @Test
    void dryRunShouldReturnPlannedCountsWithoutMutation() {
        DemoAccountCatalogService.DemoAccountDefinition general01 = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-01", "general01@demo.insidemovie.local", "일반01", "일반 계정 01", "GENERAL",
                EmotionType.JOY, 0.6f, 0.1f, 0.1f, 0.1f, 0.1f
        );
        DemoAccountCatalogService.DemoAccountDefinition general02 = new DemoAccountCatalogService.DemoAccountDefinition(
                "general-02", "general02@demo.insidemovie.local", "일반02", "일반 계정 02", "GENERAL",
                EmotionType.SADNESS, 0.1f, 0.6f, 0.1f, 0.1f, 0.1f
        );

        when(demoAccountCatalogService.getGeneralAccountDefinitions()).thenReturn(List.of(general01, general02));
        when(memberRepository.findByEmail(general01.email()))
                .thenReturn(Optional.of(Member.builder().id(1L).email(general01.email()).isBanned(false).build()));
        when(memberRepository.findByEmail(general02.email()))
                .thenReturn(Optional.of(Member.builder().id(2L).email(general02.email()).isBanned(false).build()));

        when(matchRepository.countByWinnerIdIsNotNull()).thenReturn(6L);
        when(matchRepository.countByWinnerIdIsNull()).thenReturn(0L);

        MatchSeedReport report = matchSeedService.seed(true, 8, 10);

        assertThat(report.getClosedMatchesCreated()).isEqualTo(2);
        assertThat(report.getCurrentMatchesCreated()).isEqualTo(1);
        assertThat(report.getVotesCreated()).isEqualTo(6);

        verify(matchService, never()).createMatch();
        verify(matchService, never()).closeMatch();
    }
}

