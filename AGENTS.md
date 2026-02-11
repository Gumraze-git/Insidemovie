# Insidemovie Monorepo AGENTS Guide

이 파일은 모노레포 루트 기본 지침입니다.

## Scope and Precedence

- 이 파일은 `/Users/dkim/DKim/10_Project/Insidemovie-monorepo` 전체에 적용됩니다.
- 하위 디렉터리에 `AGENTS.md` 또는 `AGENTS.override.md`가 있으면 하위 지침이 우선합니다.
- 앱별 상세 규칙은 각 앱의 `AGENTS.md`를 따릅니다.

## Commit and Branch Rules

- Conventional Commit 타입은 `feat`, `fix`, `refactor`, `chore`, `docs`를 사용합니다.
- 권장 커밋 형식: `type(scope): summary`
- 권장 scope: `frontend`, `backend`, `ai`, `monorepo`
- 브랜치는 작업 목적이 드러나게 짧고 명확하게 작성합니다.

## PR Minimum Checklist

- 변경 요약과 의도, 영향 범위를 PR 본문에 명시합니다.
- 관련 이슈를 링크합니다.
- 변경한 앱 기준 최소 검증을 완료합니다.

## App Entrypoints and Commands

| App | Path | Install | Local Run | Validation |
| --- | --- | --- | --- | --- |
| Frontend | `apps/frontend` | `npm install` | `npm run dev` | `npm run lint && npm run build` |
| Backend | `apps/backend` | `./gradlew dependencies` | `./gradlew bootRun` | `./gradlew test && ./gradlew build` |
| AI | `apps/ai` | `pip install -r requirements.txt` | `python main.py` | `python -m compileall .` |

## Issue Template Policy

- 이슈는 루트 `/.github/ISSUE_TEMPLATE/conventional-request.yml`을 사용합니다.
- 앱별 `.github/ISSUE_TEMPLATE`는 운영하지 않습니다.
