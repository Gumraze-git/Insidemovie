DOCKER_COMPOSE ?= docker compose
COMPOSE_FILE ?= docker-compose.yml
PROJECT_NAME ?= insidemovie
DC = $(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -p $(PROJECT_NAME)
BACKEND_REPORTS_VOLUME = -v $(PWD)/apps/backend/build/reports:/app/build/reports

.PHONY: help help-all prepare-model build up down logs ps clean reset-db build-toolbox \
	build-frontend build-backend-spring build-backend-ai \
	up-frontend up-backend-spring up-backend-ai \
	logs-frontend logs-backend-spring logs-backend-ai \
	seed-movie-genres seed-movie-genres-dry-run \
	seed-movie-metadata seed-movie-metadata-dry-run \
	seed-boxoffice seed-boxoffice-dry-run \
	refresh-movie-posters refresh-movie-posters-dry-run \
	audit-movie-posters audit-movie-posters-dry-run \
	seed-all seed-all-reset \
	data-backfill data-backfill-dry-run \
	seed-reviews-ai seed-reviews-ai-dry-run \
	seed-matches seed-matches-dry-run

help:
	@echo "핵심 타겟:"
	@echo "  make up                - 전체 서비스 실행(필요 시 빌드)"
	@echo "  make down              - 전체 서비스 중지/제거"
	@echo "  make logs              - 전체 로그 팔로우"
	@echo "  make ps                - 컨테이너 상태 확인"
	@echo "  make seed-all          - 통합 증분 시드 실행"
	@echo "  make seed-all-reset    - DB 초기화 후 통합 시드 실행(주의: 기존 데이터 삭제)"
	@echo "  make data-backfill-dry-run - 통합 시드 dry-run"
	@echo "  make help-all          - 전체/고급 타깃 보기"

help-all:
	@echo "전체 타겟:"
	@echo "  make prepare-model         - AI 모델 파일 상태 확인 및 필요 시 LFS pull 수행"
	@echo "  make build                 - 모든 서비스 이미지 빌드"
	@echo "  make up                    - 모든 서비스 실행(백그라운드, 필요 시 빌드)"
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
	@echo "  make seed-all              - 통합 증분 시드 실행(data-backfill 래퍼)"
	@echo "  make seed-all-reset        - DB 초기화 후 통합 시드 실행"
	@echo "  make data-backfill         - 계정/영화/리뷰-AI/대결 통합 증분 시드"
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

up: prepare-model
	$(DC) up -d --build

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

data-backfill:
	$(DC) build backend
	$(DC) up -d mysql ai
	$(DC) run --rm --no-deps $(BACKEND_REPORTS_VOLUME) backend \
		--spring.main.web-application-type=none \
		--demo.data.backfill.enabled=true \
		--demo.data.backfill.dry-run=false

seed-all:
	$(MAKE) data-backfill

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
