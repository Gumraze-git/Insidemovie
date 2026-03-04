# Insidemovie Monorepo

영화 리뷰/감정 분석 기반 추천 플랫폼 **Inside Movie**의 모노레포입니다.  
사용자 리뷰/댓글 데이터를 기반으로 감정을 분석하고, 분석 결과를 추천 로직에 반영해 맞춤형 영화를 제안합니다.

## 프로젝트 소개

- 영화 커뮤니티(리뷰/댓글) 기능과 추천 기능을 결합한 웹 플랫폼
- 리뷰/댓글 텍스트 감정 분석 결과를 추천 입력으로 활용
- 외부 영화 데이터(KOBIS/KMDb) 연동으로 박스오피스/메타데이터 제공

## 프로젝트 문서

- 프로젝트 상세(Devlog): [http://localhost:3000/projects/movie-platform](http://localhost:3000/projects/movie-platform)

## 기간

- 2025.06 ~ 2025.07

## 기술 스택

- Frontend: React, TypeScript, Vite, Tailwind CSS
- Backend: Spring Boot 3.5.3, Java 17, Spring Security, OAuth2, JPA/Hibernate
- AI: FastAPI, Python
- Data: MySQL, Redis
- Infra/Docs: Docker Compose, OpenAPI/Swagger
- External API: KOBIS, KMDb

## 내 역할

- 인증/인가 흐름 설계 및 예외 응답 표준화
- 영화/리뷰/댓글 도메인 모델링 및 CRUD API 구현
- KOBIS/KMDb 외부 연동 및 추천 입력 파이프라인 구성
- FastAPI 감정 분석 서비스 연동 및 실패 폴백 처리
- 프론트엔드 협업을 위한 API 명세 정비

## 나의 목표

- 계층 분리(Controller-Service-Repository)와 DTO 경계 명확화
- 인증/비즈니스 예외 처리 일관화로 클라이언트 연동 안정성 강화
- 외부 연동 지연/실패 대비(타임아웃, 예외 처리, 기본 추천)로 가용성 확보
- 리뷰/댓글 -> 감정 분석 -> 추천 랭킹의 End-to-End 흐름 완성

## 자주 쓰는 명령

- 전체 실행(기본 ask): `make up`
- 전체 실행(모델 필수): `MODEL=required make up`
- 전체 실행(모델 스킵, no-AI 제한 모드): `MODEL=skip make up`
- 전체 실행 + 통합 시드 강제: `SEED=yes make up`
- 전체 실행 + 시드 생략 강제: `SEED=no make up`
- 백엔드 서버 실행(Spring + AI): `make up-backend-spring && make up-backend-ai`
- Spring 서버 실행: `make up-backend-spring`
- AI 서버 실행: `make up-backend-ai`
- 중지: `make down`
- 로그(전체): `make logs`
- 로그(Spring): `make logs-backend-spring`
- 로그(AI): `make logs-backend-ai`

## 통합 데모 데이터 시드

아래 명령은 계정/영화 장르/영화 메타/리뷰+AI 감정/영화 대결 이력을 한 번에 증분 시드합니다.

- 실제 반영: `make seed-all`
- DB 초기화 후 전체 시드: `make seed-all-reset` (주의: 기존 데이터 삭제)
- 시뮬레이션: `make data-backfill-dry-run`
- 고급/세부 실행 명령: `make data-backfill`

재실행 정책:
- 기존 데이터를 삭제하지 않고 부족분만 채우는 idempotent 증분 방식

리뷰 시드 정책:
- 리뷰는 런타임 생성이 아닌 고정 fixture(`apps/backend/src/main/resources/seed/demo-reviews.v1.jsonl`)를 사용합니다.
- 면접/데모 환경에서 추가 파일 없이 동일 결과를 재현할 수 있습니다.

주의:
- Docker daemon이 꺼져 있으면 모든 `make` 명령이 실패합니다. 먼저 Docker Desktop을 실행하세요.
- 공개(public) 레포에서는 Git LFS 객체도 접근 가능한 사용자에게 다운로드될 수 있으므로, 민감 데이터는 절대 저장하지 마세요.

## 첫 실행

1. (권장) Git LFS 설치
```bash
brew install git-lfs
```

2. (권장) Git LFS 초기화
```bash
git lfs install
```

3. 전체 스택 실행
```bash
make up
```

`make up` 동작:
- `MODEL=ask|required|skip` 정책으로 모델 준비 상태를 먼저 판단합니다. (기본: `MODEL=ask`)
- 모델 준비가 완료되면 전체 모드(full)로 `mysql+ai+backend+frontend`를 기동합니다.
- 모델 미준비 + `MODEL=ask` + 대화형 터미널이면 용량/시간/수동 명령을 안내하고 `y/n` 확인 후 이번 실행은 제한 모드(no-AI)로 진행합니다.
- 모델 미준비 + 비대화식 실행(`CI=true` 또는 stdin 비-TTY) + `MODEL=ask`는 실패 종료합니다.
- 제한 모드(no-AI)에서는 `mysql+backend+frontend`만 기동하고, 시드가 필요할 때는 `seed-all-no-ai` 경로를 사용합니다.
- 이후 서비스 기동이 끝나면 `insidemovie_backend` DB를 검사해 시드 여부를 기존처럼 확인합니다. (`SEED=ask|yes|no`)

## 모델 준비(수동)

모델이 없거나 LFS 포인터 상태일 때, `make up`은 아래 명령을 안내합니다.

```bash
brew install git-lfs
git lfs install
git lfs pull --include="apps/ai/models/0717_kobert_5_emotion_model/model.safetensors"
```

- 예상 다운로드 용량: 약 352MB
- 예상 소요 시간: 약 2~10분 (네트워크 환경에 따라 변동)

## 제한 실행 모드(no-AI) 제약

- AI 컨테이너를 기동하지 않으므로 AI 의존 기능(감정 예측/추천)이 제한됩니다.
- 리뷰 AI 감정 생성 단계는 제외된 시드(`seed-all-no-ai`)만 실행됩니다.
- AI 포함 전체 기능 검증이 필요하면 모델 다운로드 후 `make up` 또는 `MODEL=required make up`을 사용하세요.

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
