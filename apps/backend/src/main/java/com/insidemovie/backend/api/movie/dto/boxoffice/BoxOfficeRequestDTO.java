package com.insidemovie.backend.api.movie.dto.boxoffice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "박스오피스 조회 요청")
public class BoxOfficeRequestDTO {
    @Schema(description = "조회 날짜 (yyyyMMdd)")
    private String targetDt;

    @Schema(description = "결과 ROW 개수 (default: 10, max: 10)")
    private Integer itemPerPage;

    @Schema(description = "다양성/상업 영화 구분 (Y: 다양성, N: 상업)", allowableValues = {"Y", "N"})
    private String multiMovieYn;

    @Schema(description = "한국/외국 영화 구분 (K: 한국, F: 외국)", allowableValues = {"K", "F"})
    private String repNationCd;

    @Schema(description = "상영지역 코드 (공통코드 조회 서비스의 지역코드)")
    private String wideAreaCd;

    @Schema(description = "주간/주말/주중 구분 (0: 주간, 1: 주말, 2: 주중). 일간 조회 시 null", allowableValues = {"0", "1", "2"})
    private String weekGb;
}
