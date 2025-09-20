# Spring Boot JAR을 빌드하고 실행하기 위한 멀티 스테이지 빌드
# 1) 빌드 단계
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
# 소스 복사 (빌드 컨텍스트는 repo/atdd-camping-reservation)
COPY . .
# 부트 실행 가능 JAR 빌드 (이미지 빌드 속도를 위해 테스트 생략)
RUN chmod +x ./gradlew && ./gradlew --no-daemon clean bootJar -x test

# 2) 런타임 단계
FROM eclipse-temurin:17-jdk
WORKDIR /app
# 빌더 단계에서 생성된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
