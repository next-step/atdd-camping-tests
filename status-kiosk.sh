#!/bin/bash

# ATDD 캠핑 키오스크 애플리케이션 상태 확인 스크립트
# Docker Compose 서비스 상태 및 로그 확인

set -e  # 에러 발생 시 스크립트 중단

COMPOSE_FILE="infra/docker-compose.yml"
CONTAINER_NAME="camping-kiosk"

echo "📊 ATDD 캠핑 키오스크 애플리케이션 상태 확인"
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

echo "🔍 Docker Compose 서비스 상태:"
docker compose -f "$COMPOSE_FILE" ps
echo ""

echo "🐳 컨테이너 상세 정보:"
if docker ps --filter "name=$CONTAINER_NAME" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -q "$CONTAINER_NAME"; then
    docker ps --filter "name=$CONTAINER_NAME" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    echo ""
    echo "💾 리소스 사용량:"
    docker stats "$CONTAINER_NAME" --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"
    
    echo ""
    echo "🌐 애플리케이션 접속 테스트:"
    if curl -f http://localhost:8080 >/dev/null 2>&1; then
        echo "✅ 애플리케이션이 정상적으로 응답합니다."
        echo "   URL: http://localhost:8080"
    else
        echo "❌ 애플리케이션이 응답하지 않습니다."
        echo "   포트 8080이 열려있는지 확인하세요."
    fi
    
    echo ""
    echo "📋 최근 로그 (마지막 50줄):"
    echo "----------------------------------------"
    docker logs "$CONTAINER_NAME" --tail 50
    echo "----------------------------------------"
    
else
    echo "❌ '$CONTAINER_NAME' 컨테이너가 실행되지 않았습니다."
    echo ""
    echo "🔍 모든 컨테이너 확인:"
    docker ps -a --filter "name=$CONTAINER_NAME" --format "table {{.Names}}\t{{.Status}}\t{{.CreatedAt}}"
fi

echo ""
echo "🌐 네트워크 상태:"
if docker network ls --filter "name=camping-kiosk-network" --format "table {{.Name}}\t{{.Driver}}\t{{.Scope}}" | grep -q "camping-kiosk-network"; then
    docker network ls --filter "name=camping-kiosk-network" --format "table {{.Name}}\t{{.Driver}}\t{{.Scope}}"
else
    echo "❌ 'camping-kiosk-network' 네트워크를 찾을 수 없습니다."
fi

echo ""
echo "💡 유용한 명령어:"
echo "   전체 로그 보기: docker logs $CONTAINER_NAME"
echo "   실시간 로그: docker logs $CONTAINER_NAME -f"
echo "   컨테이너 접속: docker exec -it $CONTAINER_NAME /bin/bash"
echo "   서비스 재시작: ./start-kiosk.sh"
echo "   서비스 중지: ./stop-kiosk.sh"
