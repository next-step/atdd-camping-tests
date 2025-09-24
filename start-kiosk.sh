#!/bin/bash

# ATDD 캠핑 키오스크 애플리케이션 기동 스크립트
# 이미지 빌드 포함하여 Docker Compose로 서비스 시작

set -e  # 에러 발생 시 스크립트 중단

COMPOSE_FILE="infra/docker-compose.yml"
CONTAINER_NAME="camping-kiosk"

echo "🚀 ATDD 캠핑 키오스크 애플리케이션을 시작합니다..."
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
    echo "❌ Docker가 실행되지 않았습니다. Docker Desktop을 시작해주세요."
    exit 1
fi

echo "🔨 이미지 빌드 및 컨테이너 시작 중..."
docker compose -f "$COMPOSE_FILE" up -d --build

echo ""
echo "⏳ 애플리케이션 시작을 기다리는 중..."
sleep 5

echo ""
echo "📊 서비스 상태 확인:"
docker compose -f "$COMPOSE_FILE" ps

echo ""
echo "📋 최근 로그 (마지막 20줄):"
docker logs "$CONTAINER_NAME" --tail 20

echo ""
echo "🌐 애플리케이션 접속 정보:"
echo "   URL: http://localhost:8080"
echo ""

# 헬스체크 확인
echo "🔍 헬스체크 확인 중..."
for i in {1..10}; do
    if curl -f http://localhost:8080 >/dev/null 2>&1; then
        echo "✅ 애플리케이션이 성공적으로 시작되었습니다!"
        echo ""
        echo "🎉 캠핑 키오스크가 준비되었습니다!"
        echo "   브라우저에서 http://localhost:8080 을 열어보세요."
        exit 0
    fi
    echo "   시도 $i/10 - 애플리케이션 시작 대기 중..."
    sleep 3
done

echo "⚠️  애플리케이션이 완전히 시작되지 않았을 수 있습니다."
echo "   로그를 확인하려면: docker logs $CONTAINER_NAME"
echo "   상태를 확인하려면: ./status-kiosk.sh"
