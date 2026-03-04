#!/usr/bin/env bash
set -euo pipefail

log_info() {
  echo "[도커] $1"
}

log_warn() {
  echo "[도커][경고] $1" >&2
}

print_guide() {
  echo "[도커] 해결 방법:"
  echo "[도커]   1) Docker Desktop(또는 Docker Engine)을 실행하세요."
  echo "[도커]   2) docker info 명령으로 정상 동작을 확인하세요."
  echo "[도커]   3) make up 명령을 다시 실행하세요."
}

if ! command -v docker >/dev/null 2>&1; then
  log_warn "docker CLI를 찾을 수 없습니다."
  print_guide
  exit 2
fi

if ! docker info >/dev/null 2>&1; then
  log_warn "Docker daemon에 연결할 수 없습니다. Docker가 실행 중인지 확인하세요."
  print_guide
  exit 1
fi

log_info "Docker daemon 연결 확인 완료"
