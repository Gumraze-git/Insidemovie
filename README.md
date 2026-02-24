# Insidemovie Monorepo

루트에서 프론트엔드, 백엔드, AI, MySQL을 함께 실행할 수 있는 로컬 개발 환경입니다.

## 첫 실행

1. Git LFS 설치
```bash
brew install git-lfs
```

2. Git LFS 초기화
```bash
git lfs install
```

3. 전체 스택 실행
```bash
make up
```

## 자주 쓰는 명령

- 전체 실행: `make up`
- 프론트 개발서버(HMR): `make up-frontend-dev`
- 백엔드(Spring)만 재기동: `make up-backend-spring`
- 중지: `make down`
- 로그: `make logs`
- DB 볼륨 초기화 후 재기동: `make reset-db`

## movie_genre 백필

추천 영화(`/recommend`)는 `movie_genre` 데이터를 사용합니다. 초기 데이터가 비어 있을 경우 아래 명령으로 KOBIS movieInfo 기반 장르 백필을 수행할 수 있습니다.

- 실제 반영: `make seed-movie-genres`
- 시뮬레이션: `make seed-movie-genres-dry-run`

## 영화 메타데이터 백필

포스터/배경 이미지/시놉시스가 누락된 영화는 KMDb 기반으로 누락건만 보강할 수 있습니다.

- 실제 반영: `make seed-movie-metadata`
- 시뮬레이션: `make seed-movie-metadata-dry-run`

필수 환경변수:
- `KOBIS_API_KEY`
- `KMDB_API_KEY`

## 에러 대응

AI 컨테이너가 모델 로드 에러로 종료되면 아래 순서로 점검합니다.

1. 모델 검증 및 자동 다운로드 시도
```bash
make prepare-model
```

2. AI 로그 확인
```bash
docker compose logs -f ai
```
