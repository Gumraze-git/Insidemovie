# Insidemovie-BE
인사이드 무비 백엔드 레포지토리

## movie_genre 백필

추천 영화(`/api/v1/movies/recommend/*`)는 `movie_genre`를 기준으로 조회됩니다.
초기 데이터에서 장르 매핑이 비어 있으면 루트에서 아래 명령으로 백필할 수 있습니다.

- `make seed-movie-genres`
- `make seed-movie-genres-dry-run`
