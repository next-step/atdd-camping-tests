#!/bin/bash

# ATDD 캠핑 키오스크 애플리케이션 종료 스크립트
# Docker Compose로 서비스 중지 및 정리

set -e  # 에러 발생 시 스크립트 중단

COMPOSE_FILE="infra/docker-compose.yml"
CONTAINER_NAME="camping-kiosk"

echo "🛑 ATDD 캠핑 키오스크 애플리케이션을 종료합니다..."
echo "📁 작업 디렉토리: $(pwd)"
echo "📄 Compose 파일: $COMPOSE_FILE"
echo ""

# Docker Compose 파일 존재 확인
if [ ! -f "$COMPOSE_FILE" ]; then
    echo "❌ Docker Compose 파일을 찾을 수 없습니다: $COMPOSE_FILE"
    exit 1
fi

# Docker가 실행 중인지 확인
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker가 실행되지 않았습니다."
    exit 1
fi

# 현재 실행 중인 서비스 확인
echo "📊 현재 서비스 상태:"
docker compose -f "$COMPOSE_FILE" ps

echo ""
echo "🔄 서비스 중지 및 컨테이너 제거 중..."
docker compose -f "$COMPOSE_FILE" down -v

echo ""
echo "🧹 정리 완료 확인:"
if docker ps -a --format "table {{.Names}}\t{{.Status}}" | grep -q "$CONTAINER_NAME"; then
    echo "⚠️  컨테이너가 아직 남아있습니다:"
    docker ps -a --filter "name=$CONTAINER_NAME" --format "table {{.Names}}\t{{.Status}}"
else
    echo "✅ 모든 컨테이너가 성공적으로 제거되었습니다."
fi

echo ""
echo "🔍 네트워크 정리 확인:"
if docker network ls --format "table {{.Name}}" | grep -q "camping-kiosk-network"; then
    echo "⚠️  네트워크가 아직 남아있습니다:"
    docker network ls --filter "name=camping-kiosk-network"
else
    echo "✅ 네트워크가 성공적으로 제거되었습니다."
fi

echo ""
echo "🎉 캠핑 키오스크 애플리케이션이 완전히 종료되었습니다!"
echo ""
echo "💡 다시 시작하려면: ./start-kiosk.sh"
