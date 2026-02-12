FROM python:3.11-slim
WORKDIR /movie

RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY . .

RUN set -eu; \
    MODEL_FILE="models/0717_kobert_5_emotion_model/model.safetensors"; \
    if [ ! -f "${MODEL_FILE}" ]; then \
      echo "[AI build] 모델 파일이 없습니다: ${MODEL_FILE}"; \
      exit 1; \
    fi; \
    if head -n 1 "${MODEL_FILE}" | grep -q "git-lfs.github.com/spec/v1"; then \
      echo "[AI build] LFS 포인터 파일 감지: ${MODEL_FILE}"; \
      echo "로컬에서 'git lfs pull --include=\"apps/ai/models/0717_kobert_5_emotion_model/model.safetensors\"' 후 다시 빌드하세요."; \
      exit 1; \
    fi; \
    SIZE_BYTES="$(wc -c < "${MODEL_FILE}")"; \
    if [ "${SIZE_BYTES}" -lt 1048576 ]; then \
      echo "[AI build] 모델 파일 크기가 비정상적으로 작습니다 (${SIZE_BYTES} bytes)."; \
      exit 1; \
    fi

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
