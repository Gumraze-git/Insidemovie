package com.insidemovie.backend.api.movie.dto.boxoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// 박스오피스 항목의 공통 속성

@Getter
@Builder
@Schema(description = "박스오피스 공통 항목")
public class BaseBoxOfficeItemDTO {
    @Schema(description = "내부 박스오피스 항목 ID")
    private Long id;

    @Schema(description = "순번")
    private String rnum;

    @Schema(description = "박스오피스 순위")
    private String rank;

    @Schema(description = "전일 대비 순위 증감분")
    private String rankInten;

    @Schema(description = "랭킹 신규 여부 (OLD: 기존, NEW: 신규)")
    private String rankOldAndNew;

    @Schema(description = "영화 대표 코드")
    private String movieCd;

    @Schema(description = "영화명(국문)")
    private String movieNm;

    @Schema(description = "개봉일")
    private String openDt;

    @Schema(description = "해당일 매출액")
    private String salesAmt;

    @Schema(description = "매출 비율")
    private String salesShare;

    @Schema(description = "전일 대비 매출액 증감분")
    private String salesInten;

    @Schema(description = "전일 대비 매출액 증감 비율")
    private String salesChange;

    @Schema(description = "누적 매출액")
    private String salesAcc;

    @Schema(description = "해당일 관객수")
    private String audiCnt;

    @Schema(description = "전일 대비 관객수 증감분")
    private String audiInten;

    @Schema(description = "전일 대비 관객수 증감 비율")
    private String audiChange;

    @Schema(description = "누적 관객수")
    private String audiAcc;

    @Schema(description = "해당일 상영 스크린 수")
    private String scrnCnt;

    @Schema(description = "해당일 상영 횟수")
    private String showCnt;

}
