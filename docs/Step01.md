# 명령어 
## 애플리케이션 기동(이미지 빌드 포함)
```
docker compose -f infra/docker-compose.yml up -d --build
```

## 상태 확인
```
docker compose -f infra/docker-compose.yml ps
docker logs infra-kiosk-1 --tail 100
```

## 종료(필요 시)
```
docker compose -f infra/docker-compose.yml down -v
```

# 트러블슈팅  
- docker 명령어를 찾지 못하는 문제 
  - commandLine 사용