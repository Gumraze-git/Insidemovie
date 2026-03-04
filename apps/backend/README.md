# Insidemovie-BE
인사이드 무비 백엔드 레포지토리

## movie_genre 백필

추천 영화(`/api/v1/movies/recommend/*`)는 `movie_genre`를 기준으로 조회됩니다.
초기 데이터에서 장르 매핑이 비어 있으면 루트에서 아래 명령으로 백필할 수 있습니다.

- `make seed-movie-genres`
- `make seed-movie-genres-dry-run`

## movie 메타데이터 백필

영화 상세의 포스터/배경/시놉시스 누락은 KMDb 연동으로 보강합니다.
누락건만 대상으로 동작하며, 재실행해도 중복 업데이트 없이 안전하게 반복 가능합니다.

- `make seed-movie-metadata`
- `make seed-movie-metadata-dry-run`
- `make audit-movie-posters`
- `make audit-movie-posters-dry-run`
- `make refresh-movie-posters`
- `make refresh-movie-posters-dry-run`
- 감사 리포트 출력: `apps/backend/build/reports/poster-audit.json`

참고:
- KOBIS/KOFIC OpenAPI는 포스터 URL을 제공하지 않습니다.
- 포스터 보강은 KMDb에서 수행하며 누락 시 프론트 fallback 이미지를 사용합니다.

## 통합 데모 데이터 백필

기본 통합 시드는 로컬 snapshot SQL을 적재하며, 외부 API(KOBIS/KMDb/FastAPI)를 호출하지 않습니다.

- `make seed-all`
- `make seed-all-no-ai`
- `make seed-all-reset` (주의: DB 볼륨 초기화 후 시드)
- `make seed-snapshot`
- `make data-backfill` (옵션: 외부 API 연동 증분 백필)
- `make data-backfill-dry-run` (옵션: 외부 API 연동 dry-run)

포함 범위:
- 데모 계정(온보딩 5 + 일반 30) 시드 보장
- 박스오피스(일간/주간) 적재
- movie_genre 백필
- movie 메타데이터(포스터/배경/시놉시스) 누락 백필
- 포스터 감사 집계 로그 출력
- 일반 계정 리뷰 + AI 감정 결과 생성
- 영화 대결 이력(닫힌 매치 8 + 현재 진행 1) 시드

단일 영역 디버그:
- `make seed-reviews-ai`
- `make seed-matches`

역대 우승 영화 데이터가 비어 있을 때:
- `make seed-matches`를 먼저 실행해 닫힌 매치(우승 확정) 이력을 보강합니다.
- 확인: `curl http://localhost:8080/api/v1/matches/winners`
- DB를 초기화했거나 전체 데이터가 비어 있으면 `make seed-all-reset` 후 재확인합니다.

리뷰 시드 정책:
- 고정 fixture 파일 `apps/backend/src/main/resources/seed/demo-reviews.v1.jsonl`을 사용합니다.
- 실행 시 생성형 호출 없이 fixture를 읽어 리뷰/감정 데이터를 적재합니다.
