package com.insidemovie.backend.api.match.repository;

import com.insidemovie.backend.api.match.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    // 최근 매치
    Optional<Match> findTopByOrderByMatchNumberDesc();

    Optional<Match> findTopByWinnerIdIsNullOrderByMatchNumberDesc();

    long countByWinnerIdIsNotNull();

    long countByWinnerIdIsNull();

    List<Match> findAllByWinnerIdIsNotNullOrderByMatchNumberDesc();

    @Query("""
            SELECT COUNT(m)
            FROM Match m
            WHERE m.winnerId IS NOT NULL
              AND EXISTS (
                    SELECT 1
                    FROM Movie mv
                    WHERE mv.id = m.winnerId
              )
            """)
    long countClosedMatchesWithExistingWinnerMovie();
}
