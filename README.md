## 🚀 테스트 환경 세팅 (원클릭)

- repos 하위 경로에 테스트에 필요한 시스템 추가
```shell
./setup.sh
```
### 테스트 실행
```shell
# 프로젝트 루트
./gradlew test
```


## 로컬 개발 환경에서 컨테이너 띄우기

### 1. DB 컨테이너 실행
```shell
cd infra && docker compose -f docker-compose-infra.yml up -d --build
```

### 2. 애플리케이션 실행
```shell
cd infra && docker compose up -d --build
```

## 컨테이너 종료시키기

### 3. 애플리케이션 종료
```shell
cd infra && docker compose down
```

### 4. DB 컨테이너 종료
```shell
cd infra && docker compose -f docker-compose-infra.yml down
```