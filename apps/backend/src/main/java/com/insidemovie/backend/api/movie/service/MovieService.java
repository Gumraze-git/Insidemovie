package com.insidemovie.backend.api.movie.service;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.constant.GenreType;
import com.insidemovie.backend.api.member.dto.emotion.EmotionAvgDTO;
import com.insidemovie.backend.api.member.entity.Member;
import com.insidemovie.backend.api.member.repository.MemberRepository;
import com.insidemovie.backend.api.movie.dto.MovieSearchResDto;
import com.insidemovie.backend.api.movie.dto.PageResDto;
import com.insidemovie.backend.api.movie.dto.SearchMovieResponseDTO;
import com.insidemovie.backend.api.movie.dto.SearchMovieWrapperDTO;
import com.insidemovie.backend.api.movie.dto.emotion.MovieEmotionResDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.entity.MovieGenre;
import com.insidemovie.backend.api.movie.repository.MovieEmotionSummaryRepository;
import com.insidemovie.backend.api.movie.repository.MovieGenreRepository;
import com.insidemovie.backend.api.movie.repository.MovieRepository;
import com.insidemovie.backend.api.review.entity.Review;
import com.insidemovie.backend.api.review.repository.EmotionRepository;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final EmotionRepository emotionRepository;
    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final MovieGenreBackfillService movieGenreBackfillService;
    private final AtomicBoolean genreBackfillAttempted = new AtomicBoolean(false);

    /**
     * TMDB 연동 제거 이후 legacy scheduler 호환용 no-op 메서드.
     */
    @Transactional
    public boolean fetchAndSaveMoviesByPage(String type, int page, boolean isInitial) {
        log.warn("[Deprecated] TMDB 연동 제거로 fetchAndSaveMoviesByPage는 동작하지 않습니다. type={}, page={}", type, page);
        return false;
    }

    /**
     * TMDB 연동 제거 이후 legacy scheduler 호환용 no-op 메서드.
     */
    @Transactional
    public void fetchAndSaveMovieById(Long externalMovieId) {
        log.warn("[Deprecated] TMDB 연동 제거로 fetchAndSaveMovieById는 동작하지 않습니다. id={}", externalMovieId);
    }

    /**
     * TMDB 연동 제거 이후 legacy 코드 호환용 no-op 메서드.
     */
    @Transactional
    public Optional<SearchMovieResponseDTO> searchMovieByTitleAndYear(String title, int year) {
        log.warn("[Deprecated] TMDB 연동 제거로 searchMovieByTitleAndYear는 빈 결과를 반환합니다. title={}, year={}", title, year);
        return Optional.empty();
    }

    public PageResDto<MovieSearchResDto> movieSearchTitle(String title, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        if (movies.isEmpty()) {
            throw new NotFoundException("제목이 '" + title + "'인 영화를 찾을 수 없습니다.");
        }

        Page<MovieSearchResDto> movieSearchResDtos = movies.map(this::convertEntityToDto);
        return new PageResDto<>(movieSearchResDtos);
    }

    /*
     * TODO: 영화 장르와, 타이틀로 검색했을때 검색되도록
     *   - "액"이 포함된 영화 타이틀을 검색하고 싶어도 액션으로 인식되어 액션 영화 나옴
     *   - 수정 방안 생각중
     */
    public PageResDto<MovieSearchResDto> searchByQuery(String q, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        List<GenreType> matched = Arrays.stream(GenreType.values())
                .filter(gt -> gt.name().contains(q))
                .toList();

        Page<Movie> moviePage;
        if (!matched.isEmpty()) {
            Page<MovieGenre> mgPage = movieGenreRepository.findByGenreTypeIn(matched, pageable);
            moviePage = mgPage.map(MovieGenre::getMovie);
        } else {
            moviePage = movieRepository.findByTitleContainingIgnoreCase(q, pageable);
        }
        Page<MovieSearchResDto> dto = moviePage.map(this::convertEntityToDto);
        return new PageResDto<>(dto);
    }

    private MovieSearchResDto convertEntityToDto(Movie movie) {
        EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());
        EmotionType mainEmotion = avg.getRepEmotionType();

        double mainEmotionValue = switch (mainEmotion) {
            case JOY -> avg.getJoy();
            case SADNESS -> avg.getSadness();
            case ANGER -> avg.getAnger();
            case FEAR -> avg.getFear();
            case DISGUST -> avg.getDisgust();
            case NONE -> 0.0;
        };

        Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
        BigDecimal rounded;
        if (ratingAvg == null || ratingAvg == 0.00) {
            rounded = BigDecimal.ZERO.setScale(2);
        } else {
            rounded = BigDecimal.valueOf(ratingAvg).setScale(2, RoundingMode.HALF_UP);
        }

        MovieSearchResDto movieSearchResDto = new MovieSearchResDto();
        movieSearchResDto.setId(movie.getId());
        movieSearchResDto.setTitle(movie.getTitle());
        movieSearchResDto.setPosterPath(movie.getPosterPath());
        movieSearchResDto.setMainEmotion(mainEmotion);
        movieSearchResDto.setMainEmotionValue(mainEmotionValue);
        movieSearchResDto.setRatingAvg(rounded);
        return movieSearchResDto;
    }

    @Transactional
    public EmotionAvgDTO getMovieEmotionSummary(Long movieId) {
        EmotionAvgDTO avg = emotionRepository.findAverageEmotionsByMovieId(movieId)
                .orElseGet(() -> EmotionAvgDTO.builder()
                        .joy(0.0).sadness(0.0).anger(0.0).fear(0.0).disgust(0.0)
                        .repEmotionType(EmotionType.DISGUST)
                        .build());

        EmotionType rep = calculateRepEmotion(avg);
        avg.setRepEmotionType(rep);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        MovieEmotionSummary summary = movieEmotionSummaryRepository
                .findByMovieId(movieId)
                .orElseGet(() -> MovieEmotionSummary.builder()
                        .movie(movie)
                        .build());

        summary.updateFromDTO(avg);
        movieEmotionSummaryRepository.save(summary);
        return avg;
    }

    EmotionType calculateRepEmotion(EmotionAvgDTO dto) {
        if (dto.getJoy() == 0.0 &&
                dto.getSadness() == 0.0 &&
                dto.getAnger() == 0.0 &&
                dto.getFear() == 0.0 &&
                dto.getDisgust() == 0.0) {
            return EmotionType.NONE;
        }

        return Map.<EmotionType, Double>of(
                        EmotionType.JOY, dto.getJoy(),
                        EmotionType.SADNESS, dto.getSadness(),
                        EmotionType.ANGER, dto.getAnger(),
                        EmotionType.FEAR, dto.getFear(),
                        EmotionType.DISGUST, dto.getDisgust()
                ).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    @Transactional
    public MovieEmotionResDTO getMovieEmotions(Long movieId) {
        return movieEmotionSummaryRepository.findByMovieId(movieId)
                .map(summary -> {
                    MovieEmotionResDTO dto = new MovieEmotionResDTO();
                    dto.setJoy(summary.getJoy());
                    dto.setSadness(summary.getSadness());
                    dto.setFear(summary.getFear());
                    dto.setAnger(summary.getAnger());
                    dto.setDisgust(summary.getDisgust());
                    dto.setDominantEmotion(EmotionType.valueOf(summary.getDominantEmotion().name()));
                    return dto;
                })
                .orElseGet(() -> {
                    MovieEmotionResDTO dto = new MovieEmotionResDTO();
                    dto.setJoy(0f);
                    dto.setSadness(0f);
                    dto.setFear(0f);
                    dto.setAnger(0f);
                    dto.setDisgust(0f);
                    dto.setDominantEmotion(EmotionType.valueOf("NONE"));
                    return dto;
                });
    }

    /**
     * DB에 저장된 영화를 popularity 내림차순으로 페이징 조회합니다.
     */
    public SearchMovieWrapperDTO getPopularMovies(int page, int pageSize) {
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.DESC, "popularity")
        );

        Page<Movie> moviePage = movieRepository.findAllByOrderByPopularityDesc(pageable);

        List<SearchMovieResponseDTO> results = moviePage.stream()
                .map(this::convertEntityToSearchMovieResponseDTO)
                .collect(Collectors.toList());

        SearchMovieWrapperDTO wrapper = new SearchMovieWrapperDTO();
        wrapper.setPage(page);
        wrapper.setResults(results);
        wrapper.setTotalPages(moviePage.getTotalPages());
        wrapper.setTotalResults((int) moviePage.getTotalElements());
        return wrapper;
    }

    private SearchMovieResponseDTO convertEntityToSearchMovieResponseDTO(Movie movie) {
        SearchMovieResponseDTO dto = new SearchMovieResponseDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackDropPath(movie.getBackdropPath());
        dto.setVoteCount(movie.getVoteCount());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setPopularity(movie.getPopularity());
        dto.setAdult(false);
        return dto;
    }

    public PageResDto<MovieSearchResDto> getRecommendedMoviesByLatest(GenreType genreType, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Movie> moviePage;
        if (isGenreDataEmpty()) {
            triggerGenreBackfillOnce();
        }

        if (isGenreDataEmpty()) {
            log.warn("[RecommendFallback] movie_genre is empty. Fallback to releaseDate sort.");
            moviePage = movieRepository.findAllByOrderByReleaseDateDesc(pageable);
        } else {
            moviePage = movieRepository.findMoviesByGenreTypeOrderByReleaseDateDesc(genreType, pageable);
        }

        if (moviePage.isEmpty()) {
            throw new NotFoundException("해당 장르의 영화가 없습니다: " + genreType.name());
        }

        return new PageResDto<>(moviePage.map(this::convertEntityToRecommendedDto));
    }

    public PageResDto<MovieSearchResDto> getRecommendedMoviesByPopularity(GenreType genreType, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Movie> moviePage;
        if (isGenreDataEmpty()) {
            triggerGenreBackfillOnce();
        }

        if (isGenreDataEmpty()) {
            log.warn("[RecommendFallback] movie_genre is empty. Fallback to popularity sort.");
            moviePage = movieRepository.findAllByOrderByPopularityDesc(pageable);
        } else {
            moviePage = movieRepository.findMoviesByGenreTypeOrderByPopularityDesc(genreType, pageable);
        }

        if (moviePage.isEmpty()) {
            throw new NotFoundException("해당 장르의 영화가 없습니다: " + genreType.name());
        }

        return new PageResDto<>(moviePage.map(this::convertEntityToRecommendedDto));
    }

    public PageResDto<MovieSearchResDto> getMyWatchedMovies(Long userId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MEMBERID_EXCEPTION.getMessage()));
        Page<Review> moviePage = reviewRepository.findByMember(member, pageable);

        Page<MovieSearchResDto> dto = moviePage.map(movieLike -> {
            Movie movie = movieLike.getMovie();
            EmotionAvgDTO avg = getMovieEmotionSummary(movie.getId());

            Double ratingAvg = reviewRepository.findAverageByMovieId(movie.getId());
            BigDecimal rounded;
            if (ratingAvg == null || ratingAvg == 0.00) {
                rounded = BigDecimal.ZERO.setScale(2);
            } else {
                rounded = BigDecimal.valueOf(ratingAvg).setScale(2, RoundingMode.HALF_UP);
            }

            EmotionType mainEmotion = avg.getRepEmotionType();
            Double mainEmotionValue = switch (mainEmotion) {
                case JOY -> avg.getJoy();
                case SADNESS -> avg.getSadness();
                case ANGER -> avg.getAnger();
                case FEAR -> avg.getFear();
                case DISGUST -> avg.getDisgust();
                default -> 0.0;
            };

            return MovieSearchResDto.builder()
                    .id(movie.getId())
                    .posterPath(movie.getPosterPath())
                    .title(movie.getTitle())
                    .mainEmotion(mainEmotion)
                    .mainEmotionValue(mainEmotionValue)
                    .ratingAvg(rounded)
                    .build();
        });
        return new PageResDto<>(dto);
    }

    /**
     * TMDB 연동 제거 이후 legacy scheduler 호환용 no-op 메서드.
     */
    @Transactional
    public int fetchTotalPages(String type) {
        log.warn("[Deprecated] TMDB 연동 제거로 fetchTotalPages는 0을 반환합니다. type={}", type);
        return 0;
    }

    private MovieSearchResDto convertEntityToRecommendedDto(Movie movie) {
        MovieSearchResDto dto = convertEntityToDto(movie);
        dto.setReleaseDate(movie.getReleaseDate());
        return dto;
    }

    private boolean isGenreDataEmpty() {
        return movieGenreRepository.count() == 0;
    }

    private void triggerGenreBackfillOnce() {
        if (!genreBackfillAttempted.compareAndSet(false, true)) {
            return;
        }
        try {
            MovieGenreBackfillReport report = movieGenreBackfillService.backfill(false);
            log.info("[RecommendFallback] auto genre backfill completed requested={} succeeded={} failed={} ignored={} mappedRows={} finalRows={}",
                    report.getRequestedMovies(),
                    report.getSucceededMovies(),
                    report.getFailedMovies(),
                    report.getIgnoredMovies(),
                    report.getMappedGenreRows(),
                    report.getFinalGenreRows());
        } catch (Exception e) {
            log.warn("[RecommendFallback] auto genre backfill failed: {}", e.getMessage());
        }
    }
}
