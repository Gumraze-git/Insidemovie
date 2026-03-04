DOCKER_COMPOSE ?= docker compose
COMPOSE_FILE ?= docker-compose.yml
PROJECT_NAME ?= insidemovie
DC = $(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -p $(PROJECT_NAME)
BACKEND_REPORTS_VOLUME = -v $(PWD)/apps/backend/build/reports:/app/build/reports
SEED ?= yes
SEED_EST_TIME ?= 약 2~5분 (네트워크 환경에 따라 달라질 수 있음)
SEED_EST_SIZE ?= 약 20~80MB (초기 데모 데이터 기준)
SEED_CHECK_TIMEOUT_SEC ?= 60
MODEL ?= ask
MODEL_EST_SIZE ?= 약 352MB
MODEL_EST_TIME ?= 약 2~10분 (네트워크에 따라 변동)
SEED_TARGET ?= seed-all

.PHONY: help help-all prepare-model build demo demo-no-ai-up up up-full up-limited up-no-ai post-up-seed down logs ps clean reset-db build-toolbox \
	build-frontend build-backend-spring build-backend-ai \
	up-frontend up-backend-spring up-backend-ai \
	logs-frontend logs-backend-spring logs-backend-ai \
	seed-movie-genres seed-movie-genres-dry-run \
	seed-movie-metadata seed-movie-metadata-dry-run \
	seed-boxoffice seed-boxoffice-dry-run \
	refresh-movie-posters refresh-movie-posters-dry-run \
	audit-movie-posters audit-movie-posters-dry-run \
	seed-snapshot seed-all seed-all-no-ai seed-all-reset \
	data-backfill data-backfill-no-ai data-backfill-dry-run \
	seed-reviews-ai seed-reviews-ai-dry-run \
	seed-matches seed-matches-dry-run

help:
	@echo "핵심 타겟:"
	@echo "  make demo              - 면접관/외부 사용자 권장 원커맨드 데모 실행(no-AI + snapshot 시드)"
	@echo "  make up                - 개발자용 전체/full 모드 실행(모델 준비 필요)"
	@echo "                           - MODEL=ask|required (기본 ask)"
	@echo "                           - SEED=yes|ask|no (기본 yes)"
	@echo "  make up-no-ai          - (호환) make demo로 위임"
	@echo "  make down              - 전체 서비스 중지/제거"
	@echo "  make logs              - 전체 로그 팔로우"
	@echo "  make ps                - 컨테이너 상태 확인"
	@echo "  make seed-all          - 로컬 snapshot 기반 통합 시드(외부 API 호출 없음)"
	@echo "  make seed-all-no-ai    - 로컬 snapshot 기반 통합 시드(외부 API 호출 없음)"
	@echo "  make seed-all-reset    - DB 초기화 후 snapshot 통합 시드(주의: 기존 데이터 삭제)"
	@echo "  make data-backfill-dry-run - 통합 시드 dry-run"
	@echo "  make help-all          - 전체/고급 타깃 보기"

help-all:
	@echo "전체 타겟:"
	@echo "  make demo                  - 사전점검 + no-AI 데모 스택 기동 + snapshot 시드"
	@echo "                              - 면접관/외부 사용자 권장 진입점"
	@echo "  make prepare-model         - AI 모델 파일 상태 확인 및 필요 시 LFS pull 수행"
	@echo "  make build                 - 모든 서비스 이미지 빌드"
	@echo "  make up                    - 개발자용 전체/full 모드 실행(모델 준비 필요)"
	@echo "  make up-no-ai              - (호환) make demo로 위임"
	@echo "                              - MODEL=ask|required (기본 ask)"
	@echo "                              - MODEL_EST_SIZE/MODEL_EST_TIME 안내 문구 오버라이드 가능"
	@echo "                              - SEED=yes|ask|no (기본 yes)"
	@echo "                              - SEED_EST_TIME/SEED_EST_SIZE/SEED_CHECK_TIMEOUT_SEC 오버라이드 가능"
	@echo "  make down                  - 컨테이너 중지 및 제거"
	@echo "  make logs                  - 서비스 로그 실시간 확인"
	@echo "  make ps                    - 컨테이너 상태 확인"
	@echo "  make clean                 - 스택 중지 후 미사용 이미지 정리"
	@echo "  make reset-db              - DB 볼륨 초기화 후 스택 재기동"
	@echo "  make build-toolbox         - 루트 유틸리티 Docker 이미지 빌드"
	@echo "  make build-frontend        - frontend 이미지 빌드"
	@echo "  make build-backend-spring  - backend(Spring) 이미지 빌드"
	@echo "  make build-backend-ai      - ai(FastAPI) 이미지 빌드"
	@echo "  make up-frontend           - frontend(정적 nginx)만 재빌드/재기동"
	@echo "  make up-backend-spring     - backend(Spring)만 재빌드/재기동"
	@echo "  make up-backend-ai         - ai(FastAPI)만 재빌드/재기동"
	@echo "  make logs-frontend         - frontend(정적 nginx) 로그 확인"
	@echo "  make logs-backend-spring   - backend(Spring) 로그 확인"
	@echo "  make logs-backend-ai       - ai(FastAPI) 로그 확인"
	@echo "  make seed-snapshot         - 로컬 snapshot(SQL) 데이터 적재"
	@echo "  make seed-all              - 로컬 snapshot 기반 통합 시드(외부 API 호출 없음)"
	@echo "  make seed-all-no-ai        - 로컬 snapshot 기반 통합 시드(외부 API 호출 없음)"
	@echo "  make seed-all-reset        - DB 초기화 후 snapshot 통합 시드"
	@echo "  make data-backfill         - 계정/영화/리뷰-AI/대결 통합 증분 시드"
	@echo "  make data-backfill-no-ai   - AI 미기동 상태에서 통합 증분 시드(리뷰 감정 fallback)"
	@echo "  make data-backfill-dry-run - 통합 시드 dry-run"
	@echo "  make seed-movie-genres     - KOBIS movieInfo 기반 movie_genre 백필 실행"
	@echo "  make seed-movie-genres-dry-run - movie_genre 백필 dry-run"
	@echo "  make seed-movie-metadata   - KMDb 기반 영화 메타 누락 백필 실행"
	@echo "  make seed-movie-metadata-dry-run - 영화 메타 누락 백필 dry-run"
	@echo "  make seed-boxoffice        - KOBIS 일/주간 박스오피스 데이터 적재"
	@echo "  make seed-boxoffice-dry-run - 박스오피스 적재 dry-run"
	@echo "  make refresh-movie-posters - 영화 포스터 메타 보강 + 감사 리포트 실행"
	@echo "  make refresh-movie-posters-dry-run - 영화 포스터 메타 보강 + 감사 리포트 dry-run"
	@echo "  make audit-movie-posters   - 포스터 누락 원인 감사 및 KMDb 기반 보강 실행"
	@echo "  make audit-movie-posters-dry-run - 포스터 누락 원인 감사 dry-run"
	@echo "  make seed-reviews-ai       - 일반계정 리뷰+AI 감정 데이터만 증분 시드"
	@echo "  make seed-reviews-ai-dry-run - 리뷰+AI 감정 데이터 dry-run"
	@echo "  make seed-matches          - 영화 대결/대결 이력 데이터만 증분 시드"
	@echo "  make seed-matches-dry-run  - 영화 대결/대결 이력 데이터 dry-run"

prepare-model:
	@./scripts/ensure-ai-model.sh

build: prepare-model
	$(DC) build

demo:
	@./scripts/ensure-demo-prereqs.sh
	@$(MAKE) demo-no-ai-up
	@echo "[데모] 실행 완료"
	@echo "[데모]   Frontend: http://localhost:5173"
	@echo "[데모]   Backend API 문서: http://localhost:8080/api-doc"

demo-no-ai-up:
	$(MAKE) up-limited
	$(MAKE) SEED=yes SEED_TARGET=seed-all-no-ai post-up-seed

up:
	@MODEL="$(MODEL)" \
	MODEL_EST_SIZE="$(MODEL_EST_SIZE)" \
	MODEL_EST_TIME="$(MODEL_EST_TIME)" \
	./scripts/decide-model-for-up.sh; \
	mode_code=$$?; \
	if [ $$mode_code -eq 0 ]; then \
		echo "[모델] 전체 모드(full)로 실행합니다."; \
		$(MAKE) up-full; \
		$(MAKE) post-up-seed; \
	elif [ $$mode_code -eq 1 ]; then \
		echo "[모델] make up은 full 전용이며, 모델이 준비되지 않아 실행을 중단합니다."; \
		echo '[모델] 데모 실행은 `make demo`를 사용하세요. (`make up-no-ai`도 동일 동작)'; \
		exit $$mode_code; \
	elif [ $$mode_code -eq 3 ]; then \
		echo "[모델] 자동 다운로드를 진행할 수 없어 make up 실행을 중단합니다."; \
		echo '[모델] 데모 실행은 `make demo`를 사용하세요. (`make up-no-ai`도 동일 동작)'; \
		exit $$mode_code; \
	elif [ $$mode_code -ge 2 ]; then \
		echo "[모델] 모델 의사결정 로직에서 오류가 발생했습니다. code=$$mode_code" >&2; \
		echo '[모델] 데모 실행은 `make demo`를 사용하세요. (`make up-no-ai`도 동일 동작)'; \
		exit $$mode_code; \
	fi

up-full:
	$(DC) up -d --build

up-limited:
	$(DC) up -d mysql
	$(DC) up -d --build --no-deps backend
	$(DC) up -d --build --no-deps frontend

up-no-ai:
	@echo "[데모][안내] make up-no-ai는 호환 명령입니다. 표준 데모 실행은 make demo를 사용하세요."
	$(MAKE) demo

post-up-seed:
	@SEED="$(SEED)" \
	SEED_TARGET="$(SEED_TARGET)" \
	SEED_EST_TIME="$(SEED_EST_TIME)" \
	SEED_EST_SIZE="$(SEED_EST_SIZE)" \
	SEED_CHECK_TIMEOUT_SEC="$(SEED_CHECK_TIMEOUT_SEC)" \
	DOCKER_COMPOSE="$(DOCKER_COMPOSE)" \
	COMPOSE_FILE="$(COMPOSE_FILE)" \
	PROJECT_NAME="$(PROJECT_NAME)" \
	./scripts/maybe-seed-after-up.sh; \
	code=$$?; \
	if [ $$code -eq 0 ]; then \
		echo "[시드] 통합 데모 데이터 시드를 시작합니다. target=$(SEED_TARGET)"; \
		$(MAKE) $(SEED_TARGET); \
	elif [ $$code -eq 1 ]; then \
		echo "[시드] 통합 데모 데이터 시드를 생략합니다."; \
	elif [ $$code -ge 2 ]; then \
		echo "[시드] 시드 의사결정 로직에서 오류가 발생했습니다. code=$$code" >&2; \
		exit $$code; \
	fi

build-frontend:
	$(DC) build frontend

build-backend-spring:
	$(DC) build backend

build-backend-ai: prepare-model
	$(DC) build ai

up-frontend:
	$(DC) up -d --build --no-deps frontend

up-backend-spring:
	$(DC) up -d --build --no-deps backend

up-backend-ai: prepare-model
	$(DC) up -d --build --no-deps ai

down:
	$(DC) down --remove-orphans

logs:
	$(DC) logs -f --tail=200

logs-frontend:
	$(DC) logs -f --tail=200 frontend

logs-backend-spring:
	$(DC) logs -f --tail=200 backend

logs-backend-ai:
	$(DC) logs -f --tail=200 ai

ps:
	$(DC) ps

clean:
	$(DC) down --remove-orphans
	docker image prune -f

reset-db:
	$(DC) down -v --remove-orphans
	$(DC) up -d --build

build-toolbox:
	docker build -t insidemovie-toolbox:local .

seed-movie-genres:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.genre.backfill.enabled=true --movie.genre.backfill.dry-run=false

seed-movie-genres-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.genre.backfill.enabled=true --movie.genre.backfill.dry-run=true

seed-movie-metadata:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.metadata.backfill.enabled=true --movie.metadata.backfill.dry-run=false

seed-movie-metadata-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.metadata.backfill.enabled=true --movie.metadata.backfill.dry-run=true

seed-boxoffice:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.boxoffice.seed.enabled=true --movie.boxoffice.seed.dry-run=false --movie.boxoffice.seed.include-daily=true --movie.boxoffice.seed.include-weekly=true --movie.boxoffice.seed.item-per-page=10 --movie.boxoffice.seed.week-gb=0

seed-boxoffice-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --spring.main.web-application-type=none --movie.boxoffice.seed.enabled=true --movie.boxoffice.seed.dry-run=true --movie.boxoffice.seed.include-daily=true --movie.boxoffice.seed.include-weekly=true --movie.boxoffice.seed.item-per-page=10 --movie.boxoffice.seed.week-gb=0

refresh-movie-posters:
	$(MAKE) seed-movie-metadata
	$(MAKE) audit-movie-posters

refresh-movie-posters-dry-run:
	$(MAKE) seed-movie-metadata-dry-run
	$(MAKE) audit-movie-posters-dry-run

audit-movie-posters:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend --spring.main.web-application-type=none --movie.metadata.poster.audit.enabled=true --movie.metadata.poster.audit.dry-run=false --movie.metadata.poster.audit.include-details=true

audit-movie-posters-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend --spring.main.web-application-type=none --movie.metadata.poster.audit.enabled=true --movie.metadata.poster.audit.dry-run=true --movie.metadata.poster.audit.include-details=true

seed-snapshot:
	DOCKER_COMPOSE="$(DOCKER_COMPOSE)" \
	COMPOSE_FILE="$(COMPOSE_FILE)" \
	PROJECT_NAME="$(PROJECT_NAME)" \
	./scripts/seed-from-snapshot.sh

data-backfill:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=false

data-backfill-no-ai:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=false \
		--demo.data.backfill.include-accounts=true \
		--demo.data.backfill.include-boxoffice=true \
		--demo.data.backfill.include-genres=true \
		--demo.data.backfill.include-metadata=true \
		--demo.data.backfill.include-poster-refresh=true \
		--demo.data.backfill.include-reviews=true \
		--demo.data.backfill.include-matches=true

seed-all:
	$(MAKE) seed-snapshot

seed-all-no-ai:
	$(MAKE) seed-snapshot

seed-all-reset:
	$(MAKE) reset-db
	$(MAKE) seed-all

data-backfill-dry-run:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=true

seed-reviews-ai:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=false \
		--demo.data.backfill.include-accounts=true \
		--demo.data.backfill.include-boxoffice=false \
		--demo.data.backfill.include-genres=false \
		--demo.data.backfill.include-metadata=false \
		--demo.data.backfill.include-poster-refresh=false \
		--demo.data.backfill.include-reviews=true \
		--demo.data.backfill.include-matches=false

seed-reviews-ai-dry-run:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=true \
		--demo.data.backfill.include-accounts=true \
		--demo.data.backfill.include-boxoffice=false \
		--demo.data.backfill.include-genres=false \
		--demo.data.backfill.include-metadata=false \
		--demo.data.backfill.include-poster-refresh=false \
		--demo.data.backfill.include-reviews=true \
		--demo.data.backfill.include-matches=false

seed-matches:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=false \
		--demo.data.backfill.include-accounts=true \
		--demo.data.backfill.include-boxoffice=false \
		--demo.data.backfill.include-genres=false \
		--demo.data.backfill.include-metadata=false \
		--demo.data.backfill.include-poster-refresh=false \
		--demo.data.backfill.include-reviews=false \
		--demo.data.backfill.include-matches=true

seed-matches-dry-run:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=true \
		--demo.data.backfill.include-accounts=true \
		--demo.data.backfill.include-boxoffice=false \
		--demo.data.backfill.include-genres=false \
		--demo.data.backfill.include-metadata=false \
		--demo.data.backfill.include-poster-refresh=false \
		--demo.data.backfill.include-reviews=false \
		--demo.data.backfill.include-matches=true
