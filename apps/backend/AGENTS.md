# Backend AGENTS Guide

이 파일은 `apps/backend` 하위 작업에 적용됩니다.

## Context

- Spring Boot + Java 17 + Gradle 기반 백엔드입니다.
- 현재 리팩토링이 진행 중이므로 구조 변경은 작게 나누어 진행합니다.

## Commands

- Run: `./gradlew bootRun`
- Test: `./gradlew test`
- Build: `./gradlew build`

로컬 프로파일이 필요한 경우 `SPRING_PROFILES_ACTIVE=local`을 사용합니다.

## Package and Design Rules

- 도메인 기준 패키지 구조(`controller`, `service`, `repository`, `entity`, `dto`)를 유지합니다.
- 구조적 변경(이동/이름 변경)과 동작 변경(기능/버그 수정)을 가능한 한 분리합니다.
- 엔티티는 명시적 상태 변경 메서드를 선호하고, 과도한 자동 setter 사용은 지양합니다.

## Testing Rules

- 새 기능은 테스트를 우선합니다.
- 버그 수정은 회귀 재현 테스트를 먼저 고려합니다.
- 리팩토링 시에는 가능한 범위에서 characterization 테스트를 우선합니다.

## External Integration Notes

- TMDB, KOBIS, FastAPI(감정 분석), Kakao OAuth 연동 포인트를 변경할 때는 관련 설정 키와 DTO를 함께 점검합니다.
- 외부 API 계약 변경 시 예외 처리와 로깅을 함께 보강합니다.
