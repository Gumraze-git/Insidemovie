# Insidemovie Monorepo

루트에서 프론트엔드, 백엔드, AI, MySQL을 함께 실행할 수 있는 로컬 개발 환경입니다.

## 첫 실행

1. Git LFS 설치
```bash
brew install git-lfs
```

2. Git LFS 초기화
```bash
git lfs install
```

3. 전체 스택 실행
```bash
make up
```

## 자주 쓰는 명령

- 전체 실행: `make up`
- AI 제외 실행: `make up-core`
- 중지: `make down`
- 로그: `make logs`
- DB 볼륨 초기화 후 재기동: `make reset-db`

## 에러 대응

AI 컨테이너가 모델 로드 에러로 종료되면 아래 순서로 점검합니다.

1. 모델 검증 및 자동 다운로드 시도
```bash
make prepare-model
```

2. AI 로그 확인
```bash
docker compose logs -f ai
```
