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

## 통합 데모 데이터 백필

백엔드/AI를 띄운 뒤 데모 필수 데이터를 증분으로 채웁니다.

- `make data-backfill`
- `make data-backfill-dry-run`

포함 범위:
- 데모 계정(온보딩 5 + 일반 30) 시드 보장
- movie_genre 백필
- movie 메타데이터(포스터/배경/시놉시스) 누락 백필
- 일반 계정 리뷰 + AI 감정 결과 생성
- 영화 대결 이력(닫힌 매치 8 + 현재 진행 1) 시드

단일 영역 디버그:
- `make seed-reviews-ai`
- `make seed-matches`
