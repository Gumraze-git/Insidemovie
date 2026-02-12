DOCKER_COMPOSE ?= docker compose
COMPOSE_FILE ?= docker-compose.yml
PROJECT_NAME ?= insidemovie
DC = $(DOCKER_COMPOSE) -f $(COMPOSE_FILE) -p $(PROJECT_NAME)

.PHONY: help prepare-env prepare-model build up up-core down restart logs ps clean reset-db build-toolbox

help:
	@echo "사용 가능한 타겟:"
	@echo "  make prepare-env   - env/backend.env가 없으면 예시 파일로 생성"
	@echo "  make prepare-model - AI 모델 파일 상태 확인 및 필요 시 LFS pull 수행"
	@echo "  make build         - 모든 서비스 이미지 빌드"
	@echo "  make up            - 모든 서비스 실행(백그라운드, 필요 시 빌드)"
	@echo "  make up-core       - AI 제외(mysql/backend/frontend) 실행"
	@echo "  make down          - 컨테이너 중지 및 제거"
	@echo "  make restart       - 모든 서비스 재시작"
	@echo "  make logs          - 서비스 로그 실시간 확인"
	@echo "  make ps            - 컨테이너 상태 확인"
	@echo "  make clean         - 스택 중지 후 미사용 이미지 정리"
	@echo "  make reset-db      - DB 볼륨 초기화 후 스택 재기동"
	@echo "  make build-toolbox - 루트 유틸리티 Docker 이미지 빌드"

prepare-env:
	@if [ ! -f env/backend.env ]; then \
		cp env/backend.env.example env/backend.env; \
		echo "Created env/backend.env from env/backend.env.example"; \
	fi

prepare-model:
	@./scripts/ensure-ai-model.sh

build: prepare-env prepare-model
	$(DC) build

up: prepare-env prepare-model
	$(DC) up -d --build

up-core: prepare-env
	$(DC) up -d mysql
	$(DC) up -d --no-deps backend frontend

down:
	$(DC) down --remove-orphans

restart:
	$(MAKE) down
	$(MAKE) up

logs:
	$(DC) logs -f --tail=200

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
