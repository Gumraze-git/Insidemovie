package com.insidemovie.backend.api.movie.dto.boxoffice;

import com.insidemovie.backend.api.constant.EmotionType;
import com.insidemovie.backend.api.movie.entity.boxoffice.DailyBoxOfficeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// 일간 박스오피스 응답 DTO
@Getter
@Builder
@Schema(description = "일간 박스오피스 응답")
public class DailyBoxOfficeResponseDTO {
    @Schema(description = "내부 영화 ID")
    private Long movieId;

    @Schema(description = "영화 제목")
    private String title;

    @Schema(description = "포스터 경로")
    private String posterPath;

    @Schema(description = "리뷰 평점 평균")
    private Double ratingAvg;

    @Schema(description = "대표 감정")
    private EmotionType mainEmotion;

    @Schema(description = "대표 감정 값")
    private Double mainEmotionValue;

    @Schema(description = "박스오피스 공통 항목")
    private BaseBoxOfficeItemDTO base;


    public static DailyBoxOfficeResponseDTO fromEntity(
            DailyBoxOfficeEntity e,
            String title,
            String posterPath,
            Double ratingAvg,
            EmotionType mainEmotion,
            Double mainEmotionValue
    ) {
        return DailyBoxOfficeResponseDTO.builder()
            .movieId(e.getMovie() != null ? e.getMovie().getId() : null)
            .base(BaseBoxOfficeItemDTO.builder()
                .id(e.getId())
                .rnum(e.getRnum())
                .rank(e.getMovieRank())
                .rankInten(e.getRankInten())
                .rankOldAndNew(e.getRankOldAndNew())
                .movieCd(e.getMovieCd())
                .movieNm(e.getMovieName())
                .openDt(e.getOpenDate())
                .salesAmt(e.getSalesAmt())
                .salesShare(e.getSalesShare())
                .salesInten(e.getSalesInten())
                .salesChange(e.getSalesChange())
                .salesAcc(e.getSalesAcc())
                .audiCnt(e.getAudiCnt())
                .audiInten(e.getAudiInten())
                .audiChange(e.getAudiChange())
                .audiAcc(e.getAudiAcc())
                .scrnCnt(e.getScrnCnt())
                .showCnt(e.getShowCnt())
            .build())
            .title(title)
            .posterPath(posterPath)
            .ratingAvg(ratingAvg)
            .mainEmotion(mainEmotion)
            .mainEmotionValue(mainEmotionValue)
            .build();
    }
}
