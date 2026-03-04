#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"

SNAPSHOT_PATH="docker/mysql/snapshots/insidemovie_backend_demo_seed.sql"

log_info() {
  echo "[데모] $1"
}

log_warn() {
  echo "[데모][경고] $1" >&2
}

fail_with_guide() {
  local reason="$1"
  shift
  log_warn "${reason}"
  echo "[데모] 해결 방법:"
  local index=1
  for step in "$@"; do
    echo "[데모]   ${index}) ${step}"
    index=$((index + 1))
  done
  exit 2
}

log_info "사전 점검을 시작합니다. (macOS/Linux 기준)"

if ! command -v docker >/dev/null 2>&1; then
  fail_with_guide \
    "docker CLI를 찾을 수 없습니다." \
    "Docker Desktop(macOS) 또는 Docker Engine+Compose 플러그인(Linux)을 설치하세요." \
    "새 터미널에서 docker --version 명령이 동작하는지 확인하세요." \
    "make demo를 다시 실행하세요."
fi

if ! docker compose version >/dev/null 2>&1; then
  fail_with_guide \
    "docker compose 명령을 사용할 수 없습니다." \
    "Docker Compose v2 플러그인을 설치하세요." \
    "docker compose version 명령이 정상 동작하는지 확인하세요." \
    "make demo를 다시 실행하세요."
fi

if ! docker info >/dev/null 2>&1; then
  fail_with_guide \
    "Docker daemon에 연결할 수 없습니다." \
    "Docker Desktop(macOS) 또는 Docker Engine(Linux)을 실행하세요." \
    "docker info 명령이 정상 동작하는지 확인하세요." \
    "make demo를 다시 실행하세요."
fi

if [[ ! -f "${SNAPSHOT_PATH}" ]]; then
  fail_with_guide \
    "snapshot 파일을 찾을 수 없습니다: ${SNAPSHOT_PATH}" \
    "최신 main 브랜치로 동기화하세요." \
    "저장소 루트에서 파일 존재 여부를 확인하세요." \
    "문제가 지속되면 저장소를 다시 clone 하세요."
fi

if git lfs version >/dev/null 2>&1; then
  log_info "git-lfs를 감지했습니다. 로컬 저장소 설정을 점검합니다."
  if git lfs install --local >/dev/null 2>&1; then
    log_info "git-lfs 로컬 설정 완료 (--local)."
  else
    log_warn "git-lfs 로컬 설정에 실패했습니다. make demo(no-AI)는 계속 진행합니다."
  fi
else
  log_info "git-lfs가 설치되어 있지 않지만 make demo(no-AI) 실행에는 필요하지 않습니다."
fi

log_info "사전 점검이 완료되었습니다."
