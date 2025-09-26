#!/bin/bash

# ATDD 캠핑 서비스 통합 관리 스크립트
# 여러 서비스(kiosk, admin, reservation)를 start/stop/status 명령으로 관리

set -e  # 에러 발생 시 스크립트 중단

COMPOSE_FILE="infra/docker-compose.yml"
INFRA_COMPOSE_FILE="infra/docker-compose-infra.yml"

# 서비스 설정 함수
get_service_info() {
    local service=$1
    case "$service" in
        kiosk)
            echo "8080:camping-kiosk"
            ;;
        admin)
            echo "8081:camping-admin"
            ;;
        reservation)
            echo "8082:camping-reservation"
            ;;
        *)
            echo ""
            ;;
    esac
}

# 사용 가능한 서비스 목록
AVAILABLE_SERVICES="kiosk admin reservation"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 도움말 출력
show_help() {
    echo "🏕️  ATDD 캠핑 서비스 관리 도구"
    echo ""
    echo "사용법: $0 <명령> [서비스명]"
    echo ""
    echo "명령:"
    echo "  start     서비스 시작 (공용 DB 자동 시작 포함)"
    echo "  stop      서비스 중지 및 정리 (all 시 공용 DB도 중지)"
    echo "  status    서비스 상태 확인"
    echo "  restart   서비스 재시작"
    echo "  logs      서비스 로그 확인"
    echo ""
    echo "서비스명 (선택사항):"
    echo "  kiosk         키오스크 서비스 (포트: 8080)"
    echo "  admin         관리자 서비스 (포트: 8081)"
    echo "  reservation   예약 서비스 (포트: 8082)"
    echo "  all           모든 서비스 (기본값)"
    echo ""
    echo "📋 주요 특징:"
    echo "  • 공용 MySQL DB(atdd-db) 자동 관리"
    echo "  • 서비스 시작 시 DB 초기화 자동 수행"
    echo "  • 모든 서비스가 공용 네트워크(atdd-net)에서 통신"
    echo ""
    echo "예시:"
    echo "  $0 start              # 공용 DB + 모든 서비스 시작"
    echo "  $0 start kiosk        # 공용 DB + 키오스크만 시작"
    echo "  $0 stop admin         # 관리자 서비스만 중지 (DB는 유지)"
    echo "  $0 stop all           # 모든 서비스 + 공용 DB 중지"
    echo "  $0 status             # 모든 서비스 상태 확인"
    echo "  $0 logs reservation   # 예약 서비스 로그 확인"
}

# Docker 환경 확인
check_docker() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        echo -e "${RED}❌ Docker Compose 파일을 찾을 수 없습니다: $COMPOSE_FILE${NC}"
        exit 1
    fi
    
    if [ ! -f "$INFRA_COMPOSE_FILE" ]; then
        echo -e "${RED}❌ 인프라 Compose 파일을 찾을 수 없습니다: $INFRA_COMPOSE_FILE${NC}"
        exit 1
    fi

    if ! docker info >/dev/null 2>&1; then
        echo -e "${RED}❌ Docker가 실행되지 않았습니다. Docker Desktop을 시작해주세요.${NC}"
        exit 1
    fi
}

# 인프라 DB 시작
start_infra_db() {
    echo -e "${BLUE}🗄️  공용 인프라 DB를 시작합니다...${NC}"
    
    # 인프라 DB가 이미 실행 중인지 확인
    if docker ps | grep -q "atdd-db"; then
        echo -e "${GREEN}✅ 인프라 DB가 이미 실행 중입니다.${NC}"
        return 0
    fi
    
    # 인프라 DB 시작
    docker compose -f "$INFRA_COMPOSE_FILE" up -d
    
    echo -e "${YELLOW}⏳ DB 초기화를 기다리는 중...${NC}"
    
    # DB가 준비될 때까지 대기 (최대 60초)
    local max_attempts=60
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if docker exec atdd-db mysqladmin ping -h127.0.0.1 -uroot -psecret --silent 2>/dev/null; then
            echo -e "${GREEN}✅ DB가 준비되었습니다!${NC}"
            break
        fi
        
        if [ $attempt -eq $max_attempts ]; then
            echo -e "${RED}❌ DB 시작 시간이 초과되었습니다.${NC}"
            exit 1
        fi
        
        echo "   시도 $attempt/$max_attempts - DB 시작 대기 중..."
        sleep 2
        ((attempt++))
    done
    
    # DB 초기화
    echo -e "${YELLOW}🔧 DB 초기화 중...${NC}"
    if [ -f "infra/db/init.sql" ]; then
        docker exec -i atdd-db mysql -uroot -psecret atdd < infra/db/init.sql 2>/dev/null || true
        echo -e "${GREEN}✅ DB 초기화가 완료되었습니다.${NC}"
    else
        echo -e "${YELLOW}⚠️  초기화 스크립트를 찾을 수 없습니다: infra/db/init.sql${NC}"
    fi
}

# 인프라 DB 중지
stop_infra_db() {
    echo -e "${BLUE}🗄️  공용 인프라 DB를 중지합니다...${NC}"
    
    if docker ps | grep -q "atdd-db"; then
        docker compose -f "$INFRA_COMPOSE_FILE" down -v
        echo -e "${GREEN}✅ 인프라 DB가 중지되었습니다.${NC}"
    else
        echo -e "${YELLOW}⚠️  인프라 DB가 실행되지 않았습니다.${NC}"
    fi
}

# 서비스 유효성 검사
validate_service() {
    local service=$1
    if [[ "$service" != "all" ]]; then
        local valid=false
        for s in $AVAILABLE_SERVICES; do
            if [[ "$s" == "$service" ]]; then
                valid=true
                break
            fi
        done
        if [[ "$valid" == "false" ]]; then
            echo -e "${RED}❌ 알 수 없는 서비스: $service${NC}"
            echo "사용 가능한 서비스: $AVAILABLE_SERVICES all"
            exit 1
        fi
    fi
}

# 서비스 시작
start_services() {
    local target_service=${1:-all}
    validate_service "$target_service"
    
    echo -e "${BLUE}🚀 ATDD 캠핑 서비스를 시작합니다...${NC}"
    echo "📁 작업 디렉토리: $(pwd)"
    echo "📄 Compose 파일: $COMPOSE_FILE"
    echo ""

    # 인프라 DB 먼저 시작
    start_infra_db
    echo ""

    if [ "$target_service" = "all" ]; then
        echo -e "${YELLOW}🔨 모든 서비스 이미지 빌드 및 컨테이너 시작 중...${NC}"
        docker compose -f "$COMPOSE_FILE" up -d --build
        
        echo ""
        echo -e "${YELLOW}⏳ 애플리케이션 시작을 기다리는 중...${NC}"
        sleep 8
        
        # 모든 서비스 헬스체크
        for service in $AVAILABLE_SERVICES; do
            check_service_health "$service"
        done
    else
        echo -e "${YELLOW}🔨 $target_service 서비스 이미지 빌드 및 컨테이너 시작 중...${NC}"
        docker compose -f "$COMPOSE_FILE" up -d --build "$target_service"
        
        echo ""
        echo -e "${YELLOW}⏳ 애플리케이션 시작을 기다리는 중...${NC}"
        sleep 5
        
        check_service_health "$target_service"
    fi
    
    echo ""
    show_service_status "$target_service"
}

# 서비스 중지
stop_services() {
    local target_service=${1:-all}
    validate_service "$target_service"
    
    echo -e "${BLUE}🛑 ATDD 캠핑 서비스를 종료합니다...${NC}"
    echo "📁 작업 디렉토리: $(pwd)"
    echo "📄 Compose 파일: $COMPOSE_FILE"
    echo ""

    # 현재 실행 중인 서비스 확인
    echo "📊 현재 서비스 상태:"
    docker compose -f "$COMPOSE_FILE" ps

    echo ""
    if [ "$target_service" = "all" ]; then
        echo -e "${YELLOW}🔄 모든 서비스 중지 및 컨테이너 제거 중...${NC}"
        docker compose -f "$COMPOSE_FILE" down -v
        echo ""
        stop_infra_db
        echo -e "${GREEN}✅ 모든 서비스가 성공적으로 종료되었습니다!${NC}"
    else
        echo -e "${YELLOW}🔄 $target_service 서비스 중지 중...${NC}"
        docker compose -f "$COMPOSE_FILE" stop "$target_service"
        docker compose -f "$COMPOSE_FILE" rm -f "$target_service"
        echo -e "${GREEN}✅ $target_service 서비스가 성공적으로 종료되었습니다!${NC}"
    fi
    
    echo ""
    echo "💡 다시 시작하려면: $0 start $target_service"
}

# 서비스 상태 확인
show_service_status() {
    local target_service=${1:-all}
    validate_service "$target_service"
    
    echo -e "${BLUE}📊 ATDD 캠핑 서비스 상태 확인${NC}"
    echo "📁 작업 디렉토리: $(pwd)"
    echo "📄 Compose 파일: $COMPOSE_FILE"
    echo ""

    echo "🔍 Docker Compose 서비스 상태:"
    docker compose -f "$COMPOSE_FILE" ps
    echo ""

    if [ "$target_service" = "all" ]; then
        for service in $AVAILABLE_SERVICES; do
            show_individual_service_status "$service"
        done
    else
        show_individual_service_status "$target_service"
    fi
    
    echo ""
    echo "💡 유용한 명령어:"
    echo "   로그 보기: $0 logs [서비스명]"
    echo "   서비스 재시작: $0 restart [서비스명]"
    echo "   서비스 중지: $0 stop [서비스명]"
}

# 개별 서비스 상태 확인
show_individual_service_status() {
    local service=$1
    local service_info=$(get_service_info "$service")
    local port=$(echo "$service_info" | cut -d: -f1)
    local container_name=$(echo "$service_info" | cut -d: -f2)
    
    echo -e "${YELLOW}🔍 $service 서비스 상태:${NC}"
    
    if docker ps --filter "name=$container_name" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -q "$container_name"; then
        docker ps --filter "name=$container_name" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
        
        echo "🌐 애플리케이션 접속 테스트:"
        if curl -f "http://localhost:$port" >/dev/null 2>&1; then
            echo -e "   ${GREEN}✅ $service 서비스가 정상적으로 응답합니다.${NC}"
            echo "   URL: http://localhost:$port"
        else
            echo -e "   ${RED}❌ $service 서비스가 응답하지 않습니다.${NC}"
            echo "   포트 $port가 열려있는지 확인하세요."
        fi
    else
        echo -e "   ${RED}❌ '$container_name' 컨테이너가 실행되지 않았습니다.${NC}"
    fi
    echo ""
}

# 서비스 헬스체크
check_service_health() {
    local service=$1
    local service_info=$(get_service_info "$service")
    local port=$(echo "$service_info" | cut -d: -f1)
    
    echo -e "${YELLOW}🔍 $service 서비스 헬스체크 확인 중...${NC}"
    for i in {1..10}; do
        if curl -f "http://localhost:$port" >/dev/null 2>&1; then
            echo -e "   ${GREEN}✅ $service 서비스가 성공적으로 시작되었습니다!${NC}"
            echo "   URL: http://localhost:$port"
            return 0
        fi
        echo "   시도 $i/10 - $service 서비스 시작 대기 중..."
        sleep 3
    done
    
    echo -e "   ${YELLOW}⚠️  $service 서비스가 완전히 시작되지 않았을 수 있습니다.${NC}"
    echo "   로그를 확인하려면: $0 logs $service"
}

# 서비스 로그 확인
show_logs() {
    local target_service=${1:-all}
    validate_service "$target_service"
    
    if [ "$target_service" = "all" ]; then
        echo -e "${BLUE}📋 모든 서비스 로그 (마지막 50줄):${NC}"
        docker compose -f "$COMPOSE_FILE" logs --tail=50
    else
        local service_info=$(get_service_info "$target_service")
        local container_name=$(echo "$service_info" | cut -d: -f2)
        
        echo -e "${BLUE}📋 $target_service 서비스 로그 (마지막 100줄):${NC}"
        echo "----------------------------------------"
        docker logs "$container_name" --tail 100
        echo "----------------------------------------"
    fi
}

# 서비스 재시작
restart_services() {
    local target_service=${1:-all}
    echo -e "${BLUE}🔄 서비스 재시작 중...${NC}"
    stop_services "$target_service"
    sleep 2
    start_services "$target_service"
}

# 메인 실행 로직
main() {
    check_docker
    
    local command=$1
    local service=$2
    
    case "$command" in
        start)
            start_services "$service"
            ;;
        stop)
            stop_services "$service"
            ;;
        status)
            show_service_status "$service"
            ;;
        restart)
            restart_services "$service"
            ;;
        logs)
            show_logs "$service"
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            echo -e "${RED}❌ 알 수 없는 명령: $command${NC}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 인자가 없으면 도움말 출력
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

main "$@"
