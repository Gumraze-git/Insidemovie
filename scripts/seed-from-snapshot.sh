#!/usr/bin/env bash
set -euo pipefail

DOCKER_COMPOSE_CMD="${DOCKER_COMPOSE:-docker compose}"
COMPOSE_FILE_PATH="${COMPOSE_FILE:-docker-compose.yml}"
PROJECT_NAME_VALUE="${PROJECT_NAME:-insidemovie}"
MYSQL_USER_VALUE="${MYSQL_USER:-root}"
MYSQL_PASSWORD_VALUE="${MYSQL_PASSWORD:-1234}"
BACKEND_DB_NAME_VALUE="${BACKEND_DB_NAME:-insidemovie_backend}"
AI_DB_NAME_VALUE="${AI_DB_NAME:-insidemovie_ai}"
WAIT_TIMEOUT_SEC="${WAIT_TIMEOUT_SEC:-60}"

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." && pwd)"

SNAPSHOT_PATH_RAW="${SNAPSHOT_PATH:-docker/mysql/snapshots/insidemovie_backend_demo_seed.sql}"
if [[ "${SNAPSHOT_PATH_RAW}" = /* ]]; then
  SNAPSHOT_PATH="${SNAPSHOT_PATH_RAW}"
else
  SNAPSHOT_PATH="${REPO_ROOT}/${SNAPSHOT_PATH_RAW}"
fi

echo_info() {
  echo "[시드][snapshot] $1"
}

echo_warn() {
  echo "[시드][snapshot][경고] $1" >&2
}

if [[ ! -f "${SNAPSHOT_PATH}" ]]; then
  echo_warn "snapshot 파일을 찾을 수 없습니다: ${SNAPSHOT_PATH}"
  exit 2
fi

read -r -a dc_parts <<< "${DOCKER_COMPOSE_CMD}"
if [[ "${#dc_parts[@]}" -eq 0 ]]; then
  echo_warn "DOCKER_COMPOSE 명령이 비어 있습니다."
  exit 2
fi

dc_cmd=("${dc_parts[@]}" -f "${COMPOSE_FILE_PATH}" -p "${PROJECT_NAME_VALUE}")

# mysql 컨테이너가 떠 있지 않아도 snapshot 적재가 가능하도록 보장한다.
"${dc_cmd[@]}" up -d mysql >/dev/null

wait_mysql_ready() {
  local deadline
  deadline=$((SECONDS + WAIT_TIMEOUT_SEC))
  while (( SECONDS < deadline )); do
    if "${dc_cmd[@]}" exec -T mysql mysqladmin ping -h localhost -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" --silent >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  return 1
}

if ! wait_mysql_ready; then
  echo_warn "MySQL 준비 상태 확인 실패 (timeout=${WAIT_TIMEOUT_SEC}s)"
  exit 2
fi

echo_info "snapshot 적재 시작: ${SNAPSHOT_PATH_RAW}"
if ! "${dc_cmd[@]}" exec -T mysql mysql -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" "${BACKEND_DB_NAME_VALUE}" < "${SNAPSHOT_PATH}"; then
  echo_warn "snapshot 적재 실패"
  exit 2
fi

echo_info "snapshot 적재 완료"

echo_info "AI 추천 원본 동기화 시작: ${BACKEND_DB_NAME_VALUE}.movie_emotion_summary -> ${AI_DB_NAME_VALUE}.movie_emotion_summary"
if ! "${dc_cmd[@]}" exec -T mysql mysql -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" -e "
DELETE FROM ${AI_DB_NAME_VALUE}.movie_emotion_summary;
INSERT INTO ${AI_DB_NAME_VALUE}.movie_emotion_summary (movie_id, joy, anger, fear, disgust, sadness)
SELECT movie_id, joy, anger, fear, disgust, sadness
FROM ${BACKEND_DB_NAME_VALUE}.movie_emotion_summary;
"; then
  echo_warn "AI 추천 원본 동기화 실패"
  exit 2
fi
echo_info "AI 추천 원본 동기화 완료"

count_pair="$("${dc_cmd[@]}" exec -T mysql mysql -N -B -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" -e "
SELECT
  (SELECT COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.movie_emotion_summary) AS backend_count,
  (SELECT COUNT(*) FROM ${AI_DB_NAME_VALUE}.movie_emotion_summary) AS ai_count;
")"

backend_emotion_summary_count="$(awk '{print $1}' <<< "${count_pair}")"
ai_emotion_summary_count="$(awk '{print $2}' <<< "${count_pair}")"

if [[ -z "${backend_emotion_summary_count}" || -z "${ai_emotion_summary_count}" ]]; then
  echo_warn "movie_emotion_summary 동기화 건수 확인 실패"
  exit 2
fi

echo_info "rows backend.movie_emotion_summary ${backend_emotion_summary_count}"
echo_info "rows ai.movie_emotion_summary ${ai_emotion_summary_count}"

if [[ "${backend_emotion_summary_count}" != "${ai_emotion_summary_count}" ]]; then
  echo_warn "movie_emotion_summary 건수 불일치: backend=${backend_emotion_summary_count}, ai=${ai_emotion_summary_count}"
  exit 2
fi

"${dc_cmd[@]}" exec -T mysql mysql -N -B -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" -e "
SELECT 'member', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.member;
SELECT 'movie', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.movie;
SELECT 'review', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.review;
SELECT 'emotion', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.emotion;
SELECT 'match', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.\`match\`;
SELECT 'vote', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.vote;
SELECT 'backend_movie_emotion_summary', COUNT(*) FROM ${BACKEND_DB_NAME_VALUE}.movie_emotion_summary;
SELECT 'ai_movie_emotion_summary', COUNT(*) FROM ${AI_DB_NAME_VALUE}.movie_emotion_summary;
" | sed 's/^/[시드][snapshot] rows /'
