plugins {
    java
}

group = "com.camping"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

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

tasks.test {
    useJUnitPlatform()
    environment("KIOSK_BASE_URL", System.getenv("KIOSK_BASE_URL")
        ?: envVars.getOrDefault("KIOSK_BASE_URL", "http://localhost:18081"))
    environment("ADMIN_BASE_URL", System.getenv("ADMIN_BASE_URL")
        ?: envVars.getOrDefault("ADMIN_BASE_URL", "http://localhost:18082"))
    environment("RESERVATION_BASE_URL", System.getenv("RESERVATION_BASE_URL")
        ?: envVars.getOrDefault("RESERVATION_BASE_URL", "http://localhost:18083"))
}

tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Run kiosk via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build", "kiosk"
    )
}

tasks.register<Exec>("kioskComposeDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(
        "docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

tasks.register<Exec>("composeUp") {
    group = "infra"
    description = "Run all services via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("composeDown") {
    group = "infra"
    description = "Stop all services and remove volumes"
    commandLine(
        "docker", "compose",
        "--env-file", ".env",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}
