package com.insidemovie.backend.api.movie.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.movie.dto.MovieDetailResDto;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeListDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.BoxOfficeRequestDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.DailyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.dto.boxoffice.WeeklyBoxOfficeResponseDTO;
import com.insidemovie.backend.api.movie.entity.Movie;
import com.insidemovie.backend.api.movie.entity.MovieEmotionSummary;
import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.entity.boxoffice.WeeklyBoxOfficeEntity;
import com.insidemovie.backend.api.movie.repository.*;
import com.insidemovie.backend.api.review.repository.ReviewRepository;
import com.insidemovie.backend.common.exception.BaseException;
import com.insidemovie.backend.common.exception.NotFoundException;
import com.insidemovie.backend.common.response.ErrorStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoxOfficeService {

    @Value("${kobis.api.key}")
    private String kobisApiKey;

    private final ObjectMapper objectMapper;
    private final DailyBoxOfficeRepository dailyRepo;
    private final WeeklyBoxOfficeRepository weeklyRepo;
    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieEmotionSummaryRepository movieEmotionSummaryRepository;
    private final ReviewRepository reviewRepository;
    @Qualifier("kobisRestClient")
    private final RestClient kobisRestClient;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String DAILY_PATH_JSON = "/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_PATH_JSON = "/boxoffice/searchWeeklyBoxOfficeList.json";

    // 일간 박스오피스 조회 및 저장
    @Transactional
    public void fetchAndStoreDailyBoxOffice(BoxOfficeRequestDTO req) {
        LocalDate date = LocalDate.now().minusDays(1);
        log.info("[Service] WILL UPSERT daily date = {}", date);
        int limit = req.getItemPerPage();

        log.info("[Daily] Fetch & Upsert for date={} (limit={})", date, limit);

        List<DailyBoxOfficeEntity> fetched = fetchDailyFromApi(date, limit);

        for (DailyBoxOfficeEntity incoming : fetched) {
            // (movieCd, targetDate) 로 기존 검색
            DailyBoxOfficeEntity entity = dailyRepo
                    .findByTargetDateAndMovieCd(date, incoming.getMovieCd())
                    .map(existing -> { existing.updateFrom(incoming); return existing; })
                    .orElse(incoming); // 새 엔티티

            dailyRepo.save(entity);
        }

        log.info("[Daily] Upsert completed (count={}) for {}", fetched.size(), date);
    }

    // 외부 API 호출하여 일간 엔티티 목록 생성
    private List<DailyBoxOfficeEntity> fetchDailyFromApi
    (
        LocalDate date,
        int itemPerPage
    ) {
        String targetDt = date.format(FMT);
        String uri = UriComponentsBuilder.fromPath(DAILY_PATH_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", targetDt)
            .queryParam("itemPerPage", itemPerPage)
            .toUriString();
        JsonNode response = kobisRestClient.get().uri(uri).retrieve().body(JsonNode.class);
        JsonNode listNode = Optional.ofNullable(response)
            .orElseGet(() -> objectMapper.createObjectNode())
            .path("boxOfficeResult")
            .path("dailyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(itemPerPage)
            .map(node -> DailyBoxOfficeEntity.builder()
                .targetDate(date)
                .rnum(node.path("rnum").asText())
                .movieRank(node.path("rank").asText())
                .rankInten(node.path("rankInten").asText())
                .rankOldAndNew(node.path("rankOldAndNew").asText())
                .movieCd(node.path("movieCd").asText())
                .movieName(node.path("movieNm").asText())
                .openDate(node.path("openDt").asText())
                .salesShare(node.path("salesShare").asText())
                .salesInten(node.path("salesInten").asText())
                .salesChange(node.path("salesChange").asText())
                .salesAcc(node.path("salesAcc").asText())
                .audiCnt(node.path("audiCnt").asText())
                .audiInten(node.path("audiInten").asText())
                .audiChange(node.path("audiChange").asText())
                .audiAcc(node.path("audiAcc").asText())
                .scrnCnt(node.path("scrnCnt").asText())
                .showCnt(node.path("showCnt").asText())
                .build())
            .collect(Collectors.toList());
    }

    // 주간 박스오피스 조회 및 저장
    @Transactional
    public void fetchAndStoreWeeklyBoxOffice(BoxOfficeRequestDTO req) {
        // 1) 지난주 날짜 & yearWeek 계산
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        WeekFields wf = WeekFields.ISO;
        int weekOfYear = lastWeek.get(wf.weekOfWeekBasedYear());
        int year       = lastWeek.get(wf.weekBasedYear());
        String yearWeek = String.format("%04dIW%02d", year, weekOfYear);

        // 2) API 호출
        List<WeeklyBoxOfficeEntity> fetched = fetchWeeklyFromApi(
            lastWeek, req.getWeekGb(), req.getItemPerPage(), yearWeek
        );

        for (WeeklyBoxOfficeEntity incoming : fetched) {
            // 3) upsert
            WeeklyBoxOfficeEntity entity = weeklyRepo
                .findByYearWeekTimeAndMovieCd(yearWeek, incoming.getMovieCd())
                .map(existing -> { existing.updateFrom(incoming); return existing; })
                .orElse(incoming);
            // 4) 저장
            weeklyRepo.save(entity);
        }
    }
    // 외부 API 호출하여 주간 엔티티 목록 생성
    private List<WeeklyBoxOfficeEntity> fetchWeeklyFromApi(
        LocalDate date,
        String weekGb,
        int itemPerPage,
        String yearWeek
    ) {
        String targetDt = date.format(FMT);
        String uri = UriComponentsBuilder
            .fromPath(WEEKLY_PATH_JSON)
            .queryParam("key", kobisApiKey)
            .queryParam("targetDt", targetDt)
            .queryParam("weekGb", weekGb)
            .queryParam("itemPerPage", itemPerPage)
            .toUriString();
        JsonNode response = kobisRestClient.get().uri(uri).retrieve().body(JsonNode.class);
        JsonNode listNode = Optional.ofNullable(response)
            .orElseGet(() -> objectMapper.createObjectNode())
            .path("boxOfficeResult")
            .path("weeklyBoxOfficeList");

        return StreamSupport.stream(listNode.spliterator(), false)
            .limit(itemPerPage)
            .map(node -> WeeklyBoxOfficeEntity.builder()
                .yearWeekTime(yearWeek)
                .rnum(node.path("rnum").asText())
                .movieRank(node.path("rank").asText())
                .rankInten(node.path("rankInten").asText())
                .rankOldAndNew(node.path("rankOldAndNew").asText())
                .movieCd(node.path("movieCd").asText())
                .movieNm(node.path("movieNm").asText())
                .openDt(node.path("openDt").asText())
                .salesAmt(node.path("salesAmt").asText())
                .salesShare(node.path("salesShare").asText())
                .salesInten(node.path("salesInten").asText())
                .salesChange(node.path("salesChange").asText())
                .salesAcc(node.path("salesAcc").asText())
                .audiCnt(node.path("audiCnt").asText())
                .audiInten(node.path("audiInten").asText())
                .audiChange(node.path("audiChange").asText())
                .audiAcc(node.path("audiAcc").asText())
                .scrnCnt(node.path("scrnCnt").asText())
                .showCnt(node.path("showCnt").asText())
                .build()
            )
            .collect(Collectors.toList());
    }

    /**
     * 저장된 일간 박스오피스 조회
     */
    @Transactional
    public BoxOfficeListDTO<DailyBoxOfficeResponseDTO> getSavedDailyBoxOffice(
            String targetDt,
            int itemPerPage
    ) {
        // 0) 요청일 파싱 (없으면 어제)
        LocalDate requestDate = (targetDt == null || targetDt.isBlank())
            ? LocalDate.now().minusDays(1)
            : LocalDate.parse(targetDt, FMT);
        String resolvedTargetDt = requestDate.format(FMT);

        // 1) 해당일 데이터 조회
        List<DailyBoxOfficeEntity> rows = dailyRepo.findAllSortedByTargetDate(requestDate);

        // 2) 없으면 최신으로 대체
        if (rows.isEmpty()) {
            List<DailyBoxOfficeEntity> latestRows = dailyRepo.findLatestSorted();
            if (!latestRows.isEmpty()) {
                LocalDate latestDate = latestRows.get(0).getTargetDate();
                if (!latestDate.equals(requestDate)) {
                    log.warn("[Daily][Fallback] 요청일 {} 데이터 없음 → 최신 {} 로 대체",
                             requestDate, latestDate);
                    rows = latestRows;
                    resolvedTargetDt = latestDate.format(FMT);
                }
            }
        }

        if (rows.isEmpty()) {
            throw new BaseException(
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getMessage()
            );
        }

        // 3) DTO 변환: movieCd(koficId) 연관 Movie 기준 메타 + 평점 평균 + 감정 통계 추가
        List<DailyBoxOfficeResponseDTO> items = rows.stream()
            .limit(itemPerPage)
            .map(e -> {
                Movie movie = e.getMovie();
                Long movieId = movie != null ? movie.getId() : null;

                // — 영화 메타정보 —
                String title       = (movie != null) ? movie.getTitle()      : e.getMovieName();
                String posterPath  = (movie != null) ? movie.getPosterPath() : null;

                // — 평점 평균 조회 & 반올림 (기존 코드 그대로) —
                Double rawRatingAvg = (movieId != null)
                    ? reviewRepository.findAverageByMovieId(movieId)
                    : null;
                BigDecimal rounded = (rawRatingAvg == null)
                    ? BigDecimal.ZERO.setScale(2)
                    : BigDecimal.valueOf(rawRatingAvg).setScale(2, RoundingMode.HALF_UP);
                double ratingValue = rounded.doubleValue();

                // — 감정 통계: movie_emotion_summary 에서 가져오기 —
                MovieEmotionSummary summary = (movieId != null)
                    ? movieEmotionSummaryRepository.findByMovieId(movieId).orElse(null)
                    : null;

                EmotionType mainEmotion = (summary != null)
                    ? summary.getDominantEmotion()
                    : EmotionType.NONE;

                double mainValue = switch (mainEmotion) {
                    case JOY     -> (summary != null ? summary.getJoy()     : 0f);
                    case SADNESS -> (summary != null ? summary.getSadness() : 0f);
                    case ANGER   -> (summary != null ? summary.getAnger()   : 0f);
                    case FEAR    -> (summary != null ? summary.getFear()    : 0f);
                    case DISGUST -> (summary != null ? summary.getDisgust() : 0f);
                    default      -> 0f;
                };

                // — 최종 DTO 생성 —
                return DailyBoxOfficeResponseDTO.fromEntity(
                    e,
                    title,
                    posterPath,
                    ratingValue,
                    mainEmotion,
                    mainValue
                );
            })
            .toList();

        // 4) BoxOfficeListDTO 반환
        return BoxOfficeListDTO.<DailyBoxOfficeResponseDTO>builder()
            .boxofficeType("일별")
            .targetDt(resolvedTargetDt)
            .items(items)
            .build();
    }

    /**
     * 저장된 주간 박스오피스 조회
     */
    @Transactional
    public BoxOfficeListDTO<WeeklyBoxOfficeResponseDTO> getSavedWeeklyBoxOffice(
            String targetDt, String weekGb, int itemPerPage) {

        String yearWeek = Optional.ofNullable(targetDt)
                .filter(s -> !s.isBlank())
                .map(dt -> {
                    LocalDate d = LocalDate.parse(dt, FMT);
                    WeekFields wf = WeekFields.ISO;
                    int w = d.get(wf.weekOfWeekBasedYear());
                    int y = d.get(wf.weekBasedYear());
                    return String.format("%04dIW%02d", y, w);
                })
                .orElse(null);

        List<WeeklyBoxOfficeEntity> rows =
                (yearWeek != null) ? weeklyRepo.findAllSortedByYearWeek(yearWeek) : List.of();

        if (rows.isEmpty()) {
            rows = weeklyRepo.findLatestSorted();
            if (rows.isEmpty()) {
                throw new BaseException(
                        ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                        ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getMessage()
                );
            }
            yearWeek = rows.get(0).getYearWeekTime();
            log.warn("[Weekly][Fallback] 요청된 targetDt 데이터 없음 → 최신 yearWeek={} 사용", yearWeek);
        }

        List<WeeklyBoxOfficeResponseDTO> items = rows.stream()
            .limit(itemPerPage)
            .map(e -> {
                Movie movie = e.getMovie();
                Long movieId = movie != null ? movie.getId() : null;

                // --- 영화 메타정보
                String title       = movie != null ? movie.getTitle()       : e.getMovieNm();
                String posterPath  = movie != null ? movie.getPosterPath()  : null;

                // --- 사용자 평점 평균 조회 & 반올림
                Double rawRatingAvg = movieId != null
                    ? reviewRepository.findAverageByMovieId(movieId)
                    : null;
                BigDecimal rounded = rawRatingAvg == null
                    ? BigDecimal.ZERO.setScale(2)
                    : BigDecimal.valueOf(rawRatingAvg).setScale(2, RoundingMode.HALF_UP);
                double ratingAvg = rounded.doubleValue();

                // --- 감정 통계(movie_emotion_summary) 조회
                MovieEmotionSummary summary = movieId != null
                    ? movieEmotionSummaryRepository.findByMovieId(movieId).orElse(null)
                    : null;
                EmotionType mainEmotion = summary != null
                    ? summary.getDominantEmotion()
                    : EmotionType.NONE;
                double mainValue = switch (mainEmotion) {
                    case JOY     -> summary.getJoy();
                    case SADNESS -> summary.getSadness();
                    case ANGER   -> summary.getAnger();
                    case FEAR    -> summary.getFear();
                    case DISGUST -> summary.getDisgust();
                    default      -> 0.0;
                };

                // --- 최종 DTO 생성
                return WeeklyBoxOfficeResponseDTO.fromEntity(
                    e,
                    title,
                    posterPath,
                    ratingAvg,
                    mainEmotion,
                    mainValue
                );
            })
            .toList();

        return BoxOfficeListDTO.<WeeklyBoxOfficeResponseDTO>builder()
            .boxofficeType("주간")
            .targetDt(yearWeek)
            .items(items)
            .build();
    }


     /**
     * 저장된 일간 박스오피스 영화의 MovieDetailResDto 리스트 반환
     */
    @Transactional
    public MovieDetailResDto getDailyMovieDetailByMovieId(Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        LocalDate baseDate = LocalDate.now().minusDays(1);
        boolean existsInYesterday = dailyRepo.existsByMovie_IdAndTargetDate(movieId, baseDate);
        if (!existsInYesterday) {
            boolean existsInLatest = dailyRepo.findLatestSorted().stream()
                    .anyMatch(row -> row.getMovie() != null && movieId.equals(row.getMovie().getId()));
            if (!existsInLatest) {
                throw new BaseException(
                        ErrorStatus.NOT_FOUND_DAILY_BOXOFFICE.getHttpStatus(),
                        "해당 영화의 일간 박스오피스 데이터가 없습니다."
                );
            }
        }
        return toMovieDetailResDto(movie);
    }

    /**
     * 저장된 주간 박스오피스 영화의 MovieDetailResDto 리스트 반환
     */
    @Transactional
    public MovieDetailResDto getWeeklyMovieDetailByMovieId(Long movieId, String weekGb) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_MOVIE_EXCEPTION.getMessage()));

        // 최신 yearWeek 결정 (weeklyRepo.findLatestSorted() 가 최신순 정렬이라고 가정)
        List<WeeklyBoxOfficeEntity> latestList = weeklyRepo.findLatestSorted();
        if (latestList.isEmpty()) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                    "저장된 주간 박스오피스 데이터가 없습니다."
            );
        }
        String latestYearWeek = latestList.get(0).getYearWeekTime();

        boolean existsInLatestWeekly = weeklyRepo.existsByMovie_IdAndYearWeekTime(movieId, latestYearWeek);
        if (!existsInLatestWeekly) {
            throw new BaseException(
                    ErrorStatus.NOT_FOUND_WEEKLY_BOXOFFICE.getHttpStatus(),
                    "최신 주간 데이터에서 해당 영화 레코드를 찾을 수 없습니다."
            );
        }
        return toMovieDetailResDto(movie);
    }


    /**
     * Movie 엔티티 → MovieDetailResDto 매핑 헬퍼
     */
    private MovieDetailResDto toMovieDetailResDto(Movie movie) {
        MovieDetailResDto dto = new MovieDetailResDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setTitleEn(movie.getTitleEn());
        dto.setOverview(movie.getOverview());
        dto.setPosterPath(movie.getPosterPath());
        dto.setBackdropPath(movie.getBackdropPath());
        dto.setOriginalLanguage(movie.getOriginalLanguage());
        dto.setIsLike(false); // TODO: 좋아요 연동

        // 장르
        List<String> genres = movieGenreRepository.findByMovie(movie)
                .stream()
                .map(mg -> mg.getGenreType().name())
                .toList();
        dto.setGenre(genres);

        // 배우 / 감독 / OTT : JSON 배열 파싱
        dto.setActors(readJsonArray(movie.getActors()));          // ["배우1","배우2",...]
        dto.setDirector(readJsonArray(movie.getDirectors()));     // ["감독1","감독2"...]
        dto.setOttProviders(readJsonArray(movie.getOttProviders())); // []

        dto.setRating(movie.getRating());

        // releaseDate (LocalDate → String "yyyy-MM-dd" 혹은 null)
        if (movie.getReleaseDate() != null) {
            dto.setReleaseDate(movie.getReleaseDate().toString());  // DTO 타입에 맞게
        } else {
            dto.setReleaseDate(null);
        }

        dto.setRuntime(movie.getRuntime());
        dto.setStatus(movie.getStatus());
        return dto;
    }


    private List<String> readJsonArray(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            // 과거 toString() 포맷 호환 (fallback)
            if (raw.startsWith("[") && raw.endsWith("]")) {
                return Arrays.stream(raw.substring(1, raw.length() - 1).split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
            }
            return List.of();
        }
    }

}
