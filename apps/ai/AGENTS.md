# AI AGENTS Guide

이 파일은 `apps/ai` 하위 작업에 적용됩니다.

## Stack

- FastAPI + Uvicorn
- PyTorch + Transformers 기반 감정 분석 모델 사용

## Environment Setup

- Conda 또는 venv 중 하나를 사용합니다.
- 기본 설치: `pip install -r requirements.txt`
- KoBERT 토크나이저가 필요하면 README의 설치 명령을 사용합니다.

## Commands

- App run (simple): `python main.py`
- App run (explicit): `uvicorn main:app --host 0.0.0.0 --port 8000 --reload`

## Model and Data Rules

- 모델 파일은 `models/`에 두고, 대용량 바이너리 변경은 의도를 명확히 기록합니다.
- 예측 응답 스키마를 바꾸면 `routers/`, `schemas.py`, 호출 클라이언트 영향 범위를 함께 점검합니다.

## Minimum Validation

- 최소 정적 검증: `python -m compileall .`
- API 라우터를 변경했으면 로컬 실행 후 기본 엔드포인트 동작을 확인합니다.
