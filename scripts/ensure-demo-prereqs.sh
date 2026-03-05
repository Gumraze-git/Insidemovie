#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"

SNAPSHOT_PATH="docker/mysql/snapshots/insidemovie_backend_demo_seed.sql"
DEMO_VERBOSE_VALUE="${DEMO_VERBOSE:-0}"

log_info() {
  echo "[데모] $1"
}

log_warn() {
  echo "[데모][경고] $1" >&2
}

log_notice() {
  echo "[데모][안내] $1"
}

log_debug() {
  if [[ "${DEMO_VERBOSE_VALUE}" == "1" ]]; then
    echo "[데모][상세] $1"
  fi
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

confirm_demo_mode() {
  log_notice "make demo는 AI 서버(ai 컨테이너)를 기동하지 않습니다."
  log_notice "AI 서버 확인이 필요하면 make up을 사용하세요."

  if [[ "${DEMO_ACK:-}" == "y" || "${DEMO_ACK:-}" == "Y" ]]; then
    log_info "안내 확인 후 진행합니다."
    return 0
  fi

  if [[ ! -t 0 ]]; then
    fail_with_guide \
      "비대화식 실행에서는 확인 입력(y)을 받을 수 없습니다." \
      "대화형 터미널에서 make demo를 실행하고 안내 확인 후 y를 입력하세요." \
      "또는 DEMO_ACK=y make demo로 비대화식 확인을 전달하세요."
  fi

  printf "[데모] 위 내용을 확인했으면 y를 입력하세요 (y/N): "
  local answer
  if ! IFS= read -r answer; then
    echo
    log_warn "입력이 종료되어 make demo 실행을 중단합니다."
    exit 1
  fi

  if [[ "${answer}" != "y" && "${answer}" != "Y" ]]; then
    log_warn "확인되지 않아 make demo 실행을 중단합니다."
    log_info "AI 서버 확인이 필요하면 make up을 사용하세요."
    exit 1
  fi

  log_info "안내 확인 후 진행합니다."
}

confirm_demo_mode

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
  log_debug "git-lfs를 감지했습니다. 로컬 저장소 설정을 점검합니다."
  if git lfs install --local >/dev/null 2>&1; then
    log_debug "git-lfs 로컬 설정 완료 (--local)."
  else
    log_warn "git-lfs 로컬 설정에 실패했습니다. make demo(no-AI)는 계속 진행합니다."
  fi
else
  log_debug "git-lfs가 설치되어 있지 않지만 make demo(no-AI) 실행에는 필요하지 않습니다."
fi

log_info "사전 점검이 완료되었습니다."
