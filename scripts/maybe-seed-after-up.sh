#!/usr/bin/env bash
set -euo pipefail

SEED_MODE_RAW="${SEED:-ask}"
SEED_MODE="$(printf '%s' "${SEED_MODE_RAW}" | tr '[:upper:]' '[:lower:]')"
SEED_TARGET_VALUE="${SEED_TARGET:-seed-all}"
SEED_EST_TIME="${SEED_EST_TIME:-약 2~5분 (네트워크 환경에 따라 달라질 수 있음)}"
SEED_EST_SIZE="${SEED_EST_SIZE:-약 20~80MB (초기 데모 데이터 기준)}"
SEED_CHECK_TIMEOUT_SEC_RAW="${SEED_CHECK_TIMEOUT_SEC:-60}"
DOCKER_COMPOSE_CMD="${DOCKER_COMPOSE:-docker compose}"
COMPOSE_FILE_PATH="${COMPOSE_FILE:-docker-compose.yml}"
PROJECT_NAME_VALUE="${PROJECT_NAME:-insidemovie}"
MYSQL_USER_VALUE="${MYSQL_USER:-root}"
MYSQL_PASSWORD_VALUE="${MYSQL_PASSWORD:-1234}"
BACKEND_DB_NAME_VALUE="${BACKEND_DB_NAME:-insidemovie_backend}"

echo_info() {
  echo "[시드] $1"
}

echo_warn() {
  echo "[시드][경고] $1" >&2
}

if [[ "${SEED_CHECK_TIMEOUT_SEC_RAW}" =~ ^[0-9]+$ ]] && [[ "${SEED_CHECK_TIMEOUT_SEC_RAW}" -gt 0 ]]; then
  SEED_CHECK_TIMEOUT_SEC_VALUE="${SEED_CHECK_TIMEOUT_SEC_RAW}"
else
  echo_warn "SEED_CHECK_TIMEOUT_SEC 값이 유효하지 않아 기본값 60초를 사용합니다: ${SEED_CHECK_TIMEOUT_SEC_RAW}"
  SEED_CHECK_TIMEOUT_SEC_VALUE=60
fi

case "${SEED_MODE}" in
  yes)
    echo_info "SEED=yes 설정으로 시드를 강제 실행합니다."
    exit 0
    ;;
  no)
    echo_info "SEED=no 설정으로 시드를 생략합니다."
    exit 1
    ;;
  ask)
    ;;
  *)
    echo_warn "SEED 값은 ask|yes|no 중 하나여야 합니다: ${SEED_MODE_RAW}"
    exit 2
    ;;
esac

if [[ "${CI:-}" == "true" ]] || [[ "${CI:-}" == "1" ]]; then
  echo_info "CI 환경에서 시드 질문을 생략합니다."
  exit 1
fi

if [[ ! -t 0 ]]; then
  echo_info "비대화식 입력(stdin 비-TTY)에서 시드 질문을 생략합니다."
  exit 1
fi

read -r -a dc_parts <<< "${DOCKER_COMPOSE_CMD}"
if [[ "${#dc_parts[@]}" -eq 0 ]]; then
  echo_warn "DOCKER_COMPOSE 명령이 비어 있습니다."
  exit 2
fi
dc_cmd=("${dc_parts[@]}" -f "${COMPOSE_FILE_PATH}" -p "${PROJECT_NAME_VALUE}")

wait_mysql_ready() {
  local deadline
  deadline=$((SECONDS + SEED_CHECK_TIMEOUT_SEC_VALUE))
  while (( SECONDS < deadline )); do
    if "${dc_cmd[@]}" exec -T mysql mysqladmin ping -h localhost -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" --silent >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done
  return 1
}

if ! wait_mysql_ready; then
  echo_warn "MySQL 준비 상태 확인에 실패하여 시드 질문을 생략합니다. (timeout=${SEED_CHECK_TIMEOUT_SEC_VALUE}s)"
  exit 1
fi

echo_info "DB 데이터 존재 여부를 확인합니다..."

read -r -d '' HAS_DATA_SQL <<SQL || true
SET SESSION group_concat_max_len = 1024000;

SELECT COUNT(*) INTO @table_count
FROM information_schema.tables
WHERE table_schema='${BACKEND_DB_NAME_VALUE}'
  AND table_type='BASE TABLE';

SELECT IF(
  @table_count = 0,
  'SELECT 0 AS has_data',
  CONCAT(
    'SELECT IF(',
    (
      SELECT GROUP_CONCAT(
        CONCAT('EXISTS (SELECT 1 FROM \`${BACKEND_DB_NAME_VALUE}\`.\`', table_name, '\` LIMIT 1)')
        SEPARATOR ' OR '
      )
      FROM information_schema.tables
      WHERE table_schema='${BACKEND_DB_NAME_VALUE}'
        AND table_type='BASE TABLE'
    ),
    ', 1, 0) AS has_data'
  )
) INTO @has_data_sql;

PREPARE stmt FROM @has_data_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
SQL

db_check_err_file="$(mktemp)"
if ! has_data_raw="$("${dc_cmd[@]}" exec -T mysql mysql -N -B -u"${MYSQL_USER_VALUE}" -p"${MYSQL_PASSWORD_VALUE}" -e "${HAS_DATA_SQL}" 2>"${db_check_err_file}")"; then
  db_check_reason="$(grep -m1 -E 'ERROR|Error|error|failed|Failed' "${db_check_err_file}" || true)"
  if [[ -z "${db_check_reason}" ]]; then
    db_check_reason="$(awk 'NF { print; exit }' "${db_check_err_file}")"
  fi
  if [[ -n "${db_check_reason}" ]]; then
    echo_warn "DB 데이터 존재 여부 검사에 실패했습니다. 원인: ${db_check_reason}"
  else
    echo_warn "DB 데이터 존재 여부 검사에 실패했습니다. 원인 로그를 확인하지 못했습니다."
  fi
  rm -f "${db_check_err_file}"
  has_data="unknown"
else
  rm -f "${db_check_err_file}"
  has_data="$(printf '%s\n' "${has_data_raw}" | awk 'NF { last=$0 } END { print last }')"
fi

case "${has_data}" in
  1)
    echo_info "데이터가 존재합니다. 시드 질문을 생략합니다."
    exit 1
    ;;
  0)
    echo_info "데이터가 존재하지 않습니다. 시드 여부를 확인합니다."
    ;;
  unknown)
    echo_warn "DB 상태를 자동 판별하지 못했습니다. 시드 여부를 확인합니다."
    ;;
  *)
    echo_warn "DB 데이터 판정 결과를 해석하지 못했습니다. 시드 여부를 확인합니다: ${has_data}"
    ;;
esac

echo_info "데모 데이터 시드를 실행할 수 있습니다."
echo_info "예상 소요 시간: ${SEED_EST_TIME}"
echo_info "예상 데이터 크기: ${SEED_EST_SIZE}"
echo_info "진행 시 make ${SEED_TARGET_VALUE} 명령이 실행됩니다."

while true; do
  printf "데모 데이터 시드를 지금 실행할까요? (y/n): "
  if ! IFS= read -r answer; then
    echo
    echo_info "입력이 종료되어 시드를 생략합니다."
    exit 1
  fi

  case "${answer}" in
    y|Y)
      echo_info "사용자 확인(y)으로 시드를 진행합니다."
      exit 0
      ;;
    n|N)
      echo_info "사용자 확인(n)으로 시드를 생략합니다."
      exit 1
      ;;
    *)
      echo "y 또는 n만 입력해 주세요."
      ;;
  esac
done
