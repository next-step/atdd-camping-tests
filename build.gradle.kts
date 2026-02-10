plugins {
    java
}

group = "com.camping"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}


//1단계: .env 파일 → envVars 맵 (Gradle 메모리)
// .env 파일에서 환경 변수 로드
val envFile = file(".env")
val envVars = mutableMapOf<String, String>()
if (envFile.exists()) {
    envFile.readLines()
        .filter { it.isNotBlank() && !it.startsWith("#") }
        .forEach { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                envVars[parts[0].trim()] = parts[1].trim()
            }
        }
}

// Versions
val cucumberVersion = "7.14.0"
val restAssuredVersion = "5.3.2"
val jacksonVersion = "2.17.2"

dependencies {
    // Cucumber
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    // RestAssured
    testImplementation("io.rest-assured:rest-assured:${restAssuredVersion}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")

    // JUnit Jupiter
    testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}


//2단계: 포트 값에서 테스트 URL을 자동 조립 → 테스트 프로세스의 OS 환경변수
tasks.test {
    useJUnitPlatform()
    val kioskPort = envVars.getOrDefault("KIOSK_HOST_PORT", "18081")
    val adminPort = envVars.getOrDefault("ADMIN_HOST_PORT", "18082")
    val reservationPort = envVars.getOrDefault("RESERVATION_HOST_PORT", "18083")

    environment("KIOSK_BASE_URL", "http://localhost:$kioskPort")
    environment("ADMIN_BASE_URL", "http://localhost:$adminPort")
    environment("RESERVATION_BASE_URL", "http://localhost:$reservationPort")
}

tasks.register<Exec>("composeUp") {
    group = "infra"
    description = "Run all services via docker compose (build + up)"
    commandLine(
        "/usr/local/bin/docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("composeDown") {
    group = "infra"
    description = "Stop all services and remove volumes"
    commandLine(
        "/usr/local/bin/docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (DB + network)"
    commandLine(
        "/usr/local/bin/docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose-infra.yml",
        "up", "-d"
    )
}

tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine(
        "/usr/local/bin/docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose-infra.yml",
        "down", "-v"
    )
}

tasks.register<Exec>("cloneRepos") {
    group = "setup"
    description = "Clone microservice repositories"
    commandLine("bash", "-c", """
        mkdir -p repos && cd repos
        [ -d atdd-camping-kiosk ] || git clone https://github.com/next-step/atdd-camping-kiosk.git
        [ -d atdd-camping-admin ] || git clone -b heeun98 https://github.com/next-step/atdd-camping-admin.git
        [ -d atdd-camping-reservation ] || git clone -b heeun98 https://github.com/next-step/atdd-camping-reservation.git
    """.trimIndent())
}
