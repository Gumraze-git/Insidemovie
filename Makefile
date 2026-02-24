DOCKER_COMPOSE ?= docker compose
COMPOSE_FILE ?= docker-compose.yml
PROJECT_NAME ?= insidemovie
DC = $(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -p $(PROJECT_NAME)

.PHONY: help prepare-model build up down restart logs ps clean reset-db build-toolbox \
	build-frontend build-backend-spring build-backend-ai \
	up-frontend up-frontend-dev up-backend-spring up-backend-ai \
	restart-frontend restart-backend-spring restart-backend-ai \
	logs-frontend logs-frontend-dev logs-backend-spring logs-backend-ai \
	seed-movie-genres seed-movie-genres-dry-run \
	seed-movie-metadata seed-movie-metadata-dry-run

help:
	@echo "사용 가능한 타겟:"
	@echo "  make prepare-model - AI 모델 파일 상태 확인 및 필요 시 LFS pull 수행"
	@echo "  make build         - 모든 서비스 이미지 빌드"
	@echo "  make up            - 모든 서비스 실행(백그라운드, 필요 시 빌드)"
	@echo "  make build-frontend      - frontend 이미지 빌드"
	@echo "  make build-backend-spring - backend(Spring) 이미지 빌드"
	@echo "  make build-backend-ai     - ai(FastAPI) 이미지 빌드"
	@echo "  make up-frontend          - frontend(정적 nginx)만 재빌드/재기동"
	@echo "  make up-frontend-dev      - frontend(Vite HMR) 개발 서버 실행"
	@echo "  make up-backend-spring    - backend(Spring)만 재빌드/재기동"
	@echo "  make up-backend-ai        - ai(FastAPI)만 재빌드/재기동"
	@echo "  make restart-frontend       - frontend만 재시작"
	@echo "  make restart-backend-spring - backend(Spring)만 재시작"
	@echo "  make restart-backend-ai     - ai(FastAPI)만 재시작"
	@echo "  make logs-frontend       - frontend(정적 nginx) 로그 확인"
	@echo "  make logs-frontend-dev   - frontend(Vite HMR) 로그 확인"
	@echo "  make logs-backend-spring - backend(Spring) 로그 확인"
	@echo "  make logs-backend-ai     - ai(FastAPI) 로그 확인"
	@echo "  make down          - 컨테이너 중지 및 제거"
	@echo "  make restart       - 모든 서비스 재시작"
	@echo "  make logs          - 서비스 로그 실시간 확인"
	@echo "  make ps            - 컨테이너 상태 확인"
	@echo "  make clean         - 스택 중지 후 미사용 이미지 정리"
	@echo "  make reset-db      - DB 볼륨 초기화 후 스택 재기동"
	@echo "  make build-toolbox - 루트 유틸리티 Docker 이미지 빌드"
	@echo "  make seed-movie-genres       - KOBIS movieInfo 기반 movie_genre 백필 실행"
	@echo "  make seed-movie-genres-dry-run - DB 저장 없이 movie_genre 백필 dry-run 실행"
	@echo "  make seed-movie-metadata       - KMDb 기반 영화 메타(포스터/시놉시스/배경) 누락 백필 실행"
	@echo "  make seed-movie-metadata-dry-run - DB 저장 없이 영화 메타 누락 백필 dry-run 실행"

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

up-frontend-dev:
	-$(DC) stop frontend
	-$(DC) rm -f frontend
	$(DC) --profile dev up -d frontend-dev

up-backend-spring:
	$(DC) up -d --build --no-deps backend

up-backend-ai: prepare-model
	$(DC) up -d --build --no-deps ai

down:
	$(DC) down --remove-orphans

restart:
	$(MAKE) down
	$(MAKE) up

restart-frontend:
	$(DC) restart frontend

restart-backend-spring:
	$(DC) restart backend

restart-backend-ai:
	$(DC) restart ai

logs:
	$(DC) logs -f --tail=200

logs-frontend:
	$(DC) logs -f --tail=200 frontend

logs-frontend-dev:
	$(DC) logs -f --tail=200 frontend-dev

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
	$(DC) run --rm --no-deps backend --movie.genre.backfill.enabled=true --movie.genre.backfill.dry-run=false

seed-movie-genres-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --movie.genre.backfill.enabled=true --movie.genre.backfill.dry-run=true

seed-movie-metadata:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --movie.metadata.backfill.enabled=true --movie.metadata.backfill.dry-run=false

seed-movie-metadata-dry-run:
	$(DC) build backend
	$(DC) up -d mysql
	$(DC) run --rm --no-deps backend --movie.metadata.backfill.enabled=true --movie.metadata.backfill.dry-run=true
