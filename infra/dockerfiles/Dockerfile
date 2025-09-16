###########
# Builder #
###########
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /workspace

# Gradle wrapper 먼저 복사 및 캐싱
COPY gradlew ./
COPY gradle/ gradle/
RUN chmod +x ./gradlew

# Build script 복사 및 의존성 다운로드 (캐시 최적화)
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon dependencies

# 소스 코드 복사 및 빌드
COPY . .
RUN ./gradlew --no-daemon clean bootJar -x test

############
# Runtime  #
############
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드 결과 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# 실행
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]
