###########
# Builder #
###########
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# Gradle Wrapper와 빌드 스크립트 먼저 복사
COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
COPY build.gradle.kts settings.gradle ./

# 줄바꿈 문자 변환 및 실행 권한 부여
RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew

# 복사된 파일 확인 (디버깅용)
RUN ls -la ./gradlew && head -1 ./gradlew

# 소스 코드 복사
COPY src ./src/

# 의존성 다운로드 및 테스트 컴파일
RUN ./gradlew --no-daemon dependencies testClasses

############
# Runtime  #
############
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 프로젝트 전체 복사 (테스트 실행용)
COPY --from=builder /workspace ./

# 테스트 실행
ENTRYPOINT ["sh", "-c", "./gradlew test"]
