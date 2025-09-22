# Build stage
FROM eclipse-temurin:17-jdk AS build
ARG SERVICE_NAME
WORKDIR /home/gradle/project
COPY ./repos/atdd-camping-${SERVICE_NAME} /home/gradle/project

# CRLF를 LF로 변환 후 실행
RUN sed -i 's/\r$//' ./gradlew && \
    chmod +x ./gradlew && \
    ./gradlew bootJar

# Package stage
FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
