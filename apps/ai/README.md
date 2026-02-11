# Inside movie AI

## 1. 가상 환경 및 패키지 간단 설치 방법
```bash
# 프로젝트 루트에 environment.yml이 있어야 합니다.
conda env create -n Insidemovie-AI -f environment.yml
```

**환경 활성화**
```bash
conda activate Insidemovie-AI
```

- - -
## 2. Conda 환경 설치 방법
### Conda 가상 환경 생성
```bash
conda create -n Insidemovie-AI python=3.12.11
```

### 생성한 가상 환경 실행
```bash
conda activate Insidemovie-AI
```
## 3. venv 환경에서 설치 방법
- `Insidemovie-AI` 이름으로 가상환경 생성
```bash
python3 -m venv --prompt Insidemovie-AI .venv
```

- 환경 활성화
```bash
source .venv/bin/activate
```


## 4. 패키지 요구사항
- uvicorn == 0.35.0
- fastapi == 0.116.1
- pytorch == 2.7.1
- transformers == 4.53.2
- pydantic-settings
- sentencepiece == 0.2.0
- motor

#### 패키지 설치 코드
```bash
pip install \
  uvicorn==0.35.0 \
  fastapi==0.116.1 \
  torch==2.7.1 \
  transformers==4.53.2 \
  pydantic-settings \
  sentencepiece==0.2.0 \
  motor 
```

#### KoBERT 설치
```bash
pip install 'git+https://github.com/SKTBrain/KoBERT.git#egg=kobert_tokenizer&subdirectory=kobert_hf'
```