# 인수테스트 레포지토리

## 역할
`admin`, `kiosk`, `reservation` 의 통합 테스트를 제공합니다. 

## 구동방식
1. `./gradlew build` 실행시 `/repos` 하위에 `atdd-camping-kiosk` 프로젝트가 클론됩니다.

## kiosk 이미지 띄우기 (docker build)
### 이미지 빌드
docker build -f infra/dockerfiles/Dockerfile-kiosk -t camping-kiosk:latest .

### 컨테이너 실행
docker run -d -p 8080:8080 --name camping-kiosk camping-kiosk:latest