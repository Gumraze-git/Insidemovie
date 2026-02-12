#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${ROOT_DIR}"

MODEL_PATH="apps/ai/models/0717_kobert_5_emotion_model/model.safetensors"
POINTER_SIGNATURE="version https://git-lfs.github.com/spec/v1"
MIN_SIZE_BYTES=1048576

fail() {
  echo "[모델 검증 실패] $1" >&2
  echo "해결 방법:" >&2
  echo "  1) brew install git-lfs" >&2
  echo "  2) git lfs install" >&2
  echo "  3) git lfs pull --include=\"${MODEL_PATH}\"" >&2
  exit 1
}

if [[ ! -f "${MODEL_PATH}" ]]; then
  fail "모델 파일이 없습니다: ${MODEL_PATH}"
fi

is_pointer_file() {
  local first_line
  first_line="$(head -n 1 "${MODEL_PATH}" 2>/dev/null || true)"
  [[ "${first_line}" == "${POINTER_SIGNATURE}" ]]
}

if is_pointer_file; then
  echo "[모델 준비] LFS 포인터 파일을 감지했습니다. 실제 모델 다운로드를 시도합니다."
  if ! git lfs version >/dev/null 2>&1; then
    fail "git-lfs가 설치되어 있지 않습니다."
  fi

  if ! git lfs pull --include="${MODEL_PATH}" --exclude="" >/dev/null 2>&1; then
    fail "git lfs pull 실행에 실패했습니다."
  fi
fi

if is_pointer_file; then
  fail "모델 파일이 여전히 LFS 포인터 상태입니다."
fi

size_bytes="$(wc -c < "${MODEL_PATH}" | tr -d ' ')"
if [[ "${size_bytes}" -lt "${MIN_SIZE_BYTES}" ]]; then
  fail "모델 파일 크기가 비정상적으로 작습니다 (${size_bytes} bytes)."
fi

echo "[모델 준비] 완료: ${MODEL_PATH} (${size_bytes} bytes)"
