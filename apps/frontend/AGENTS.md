# Frontend AGENTS Guide

이 파일은 `apps/frontend` 하위 작업에 적용됩니다.

## Stack

- React 19 + TypeScript + Vite
- ESLint 사용

## Commands

- Install: `npm install`
- Dev server: `npm run dev`
- Lint: `npm run lint`
- Build: `npm run build`
- Preview: `npm run preview`

## Working Rules

- `src/` 변경 시 타입/빌드 에러가 없어야 합니다.
- API 인터페이스 변경 시 `src/api/` 호출부와 관련 타입을 같이 정리합니다.
- 정적 파일은 `public/` 또는 `src/assets/` 목적에 맞게 배치합니다.

## Minimum Validation

- 최소 `npm run lint && npm run build`를 통과해야 합니다.
