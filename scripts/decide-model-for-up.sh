#!/usr/bin/env bash
set -euo pipefail

MODEL_MODE_RAW="${MODEL:-ask}"
MODEL_MODE="$(printf '%s' "${MODEL_MODE_RAW}" | tr '[:upper:]' '[:lower:]')"
MODEL_EST_SIZE_VALUE="${MODEL_EST_SIZE:-약 352MB}"
MODEL_EST_TIME_VALUE="${MODEL_EST_TIME:-약 2~10분 (네트워크에 따라 변동)}"
MODEL_PATH_VALUE="${MODEL_PATH:-apps/ai/models/0717_kobert_5_emotion_model/model.safetensors}"
POINTER_SIGNATURE="version https://git-lfs.github.com/spec/v1"
MIN_SIZE_BYTES=1048576

MODEL_ISSUE=""
MODEL_FILE_SIZE_BYTES=0

log_info() {
  echo "[모델] $1"
}

log_warn() {
  echo "[모델][경고] $1" >&2
}

print_manual_download_guide() {
  echo "[모델] 수동 다운로드 절차:"
  echo "[모델]   1) brew install git-lfs"
  echo "[모델]   2) git lfs install"
  echo "[모델]   3) git lfs pull --include=\"${MODEL_PATH_VALUE}\""
}

print_no_ai_guide() {
  echo '[모델] 데모 실행은 `make demo`를 사용하세요.'
}

try_auto_download_model() {
  if ! git lfs version >/dev/null 2>&1; then
    log_warn "git-lfs가 설치되어 있지 않아 자동 다운로드를 진행할 수 없습니다."
    return 10
  fi

  log_info "git lfs pull로 모델 자동 다운로드를 시도합니다..."
  if ! git lfs pull --include="${MODEL_PATH_VALUE}" --exclude="" >/dev/null 2>&1; then
    log_warn "git lfs pull 실행에 실패했습니다."
    return 1
  fi

  if is_model_ready; then
    log_info "모델 자동 다운로드 완료: ${MODEL_PATH_VALUE} (${MODEL_FILE_SIZE_BYTES} bytes)"
    return 0
  fi

  log_warn "자동 다운로드 이후에도 모델이 준비되지 않았습니다: ${MODEL_ISSUE}"
  return 1
}

is_model_ready() {
  if [[ ! -f "${MODEL_PATH_VALUE}" ]]; then
    MODEL_ISSUE="모델 파일이 없습니다: ${MODEL_PATH_VALUE}"
    return 1
  fi

  local first_line
  first_line="$(head -n 1 "${MODEL_PATH_VALUE}" 2>/dev/null || true)"
  if [[ "${first_line}" == "${POINTER_SIGNATURE}" ]]; then
    MODEL_ISSUE="LFS 포인터 파일 상태입니다: ${MODEL_PATH_VALUE}"
    return 1
  fi

  local size_bytes
  size_bytes="$(wc -c < "${MODEL_PATH_VALUE}" | tr -d ' ')"
  if [[ "${size_bytes}" -lt "${MIN_SIZE_BYTES}" ]]; then
    MODEL_ISSUE="모델 파일 크기가 비정상적으로 작습니다 (${size_bytes} bytes)"
    return 1
  fi

  MODEL_FILE_SIZE_BYTES="${size_bytes}"
  return 0
}

case "${MODEL_MODE}" in
  ask|required|skip)
    ;;
  *)
    log_warn "MODEL 값은 ask|required|skip 중 하나여야 합니다: ${MODEL_MODE_RAW}"
    exit 2
    ;;
esac

if [[ "${MODEL_MODE}" == "skip" ]]; then
  log_warn "MODEL=skip은 make up(full 전용)에서 지원하지 않습니다."
  print_no_ai_guide
  log_info "요청에 따라 이번 make up 실행을 중단합니다."
  exit 1
fi

if is_model_ready; then
  log_info "모델 준비 완료: ${MODEL_PATH_VALUE} (${MODEL_FILE_SIZE_BYTES} bytes)"
  exit 0
fi

if [[ "${MODEL_MODE}" == "required" ]]; then
  log_warn "${MODEL_ISSUE}"
  print_manual_download_guide
  exit 2
fi

if [[ "${CI:-}" == "true" ]] || [[ "${CI:-}" == "1" ]] || [[ ! -t 0 ]]; then
  log_warn "모델이 준비되지 않았고 비대화식 실행이라 ask 모드를 진행할 수 없습니다."
  log_warn "${MODEL_ISSUE}"
  print_manual_download_guide
  exit 2
fi

log_info "모델이 준비되지 않았습니다."
log_info "원인: ${MODEL_ISSUE}"
log_info "예상 다운로드 용량: ${MODEL_EST_SIZE_VALUE}"
log_info "예상 소요 시간: ${MODEL_EST_TIME_VALUE}"
print_manual_download_guide

while true; do
  printf "모델 다운로드 절차를 진행할까요? (y/n): "
  if ! IFS= read -r answer; then
    echo
    log_warn "입력이 종료되어 이번 make up 실행을 중단합니다."
    print_no_ai_guide
    exit 1
  fi

  case "${answer}" in
    y|Y)
      log_info "다운로드 진행 의사를 확인했습니다."
      if try_auto_download_model; then
        log_info "이번 실행을 전체 모드(full)로 계속 진행합니다."
        exit 0
      else
        download_code=$?
        if [[ ${download_code} -eq 10 ]]; then
          log_info "git-lfs를 설치하지 않아 자동 다운로드를 진행할 수 없습니다."
          print_manual_download_guide
          print_no_ai_guide
          log_info "요청에 따라 이번 make up 실행을 중단합니다."
          exit 3
        fi
      fi
      log_info "안내한 명령을 수동 실행한 뒤 다시 make up을 실행하면 전체 모드(full)로 기동할 수 있습니다."
      print_no_ai_guide
      log_info "요청에 따라 이번 make up 실행을 중단합니다."
      exit 1
      ;;
    n|N)
      log_info "다운로드를 생략했습니다. 이번 make up 실행을 중단합니다."
      print_no_ai_guide
      exit 1
      ;;
    *)
      echo "y 또는 n만 입력해 주세요."
      ;;
  esac
done
