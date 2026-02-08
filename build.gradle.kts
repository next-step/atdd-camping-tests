plugins {
    java
}

group = "com.camping"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// Versions
val cucumberVersion = "7.14.0"
val restAssuredVersion = "5.3.2"
val jacksonVersion = "2.17.2"

// Environment
val kioskBaseUrl: String = System.getenv("KIOSK_BASE_URL") ?: "http://localhost:18080"

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
    dependsOn("composeUp", "waitForServices")
    finalizedBy("composeDown")
    systemProperty("kiosk.base.url", kioskBaseUrl)
}

// ========== Docker Compose Tasks ==========

tasks.register<Exec>("composeUp") {
    description = "Start test environment"
    commandLine(
        "sh", "-c",
        "docker compose -f infra/docker-compose-infra.yml up -d && " +
        "docker compose -f infra/docker-compose.yml up -d --build"
    )
}

tasks.register<Exec>("waitForServices") {
    description = "Wait for services to be ready"
    dependsOn("composeUp")
    commandLine(
        "sh", "-c",
        """
        BASE_URL="${kioskBaseUrl}"
        for i in $(seq 1 30); do
            if curl -s -o /dev/null -w '%{http_code}' ${'$'}BASE_URL/health | grep -q '200'; then
                echo "Service is ready after ${'$'}i attempts"
                exit 0
            fi
            echo "Waiting for service... attempt ${'$'}i/30"
            sleep 2
        done
        echo "Service did not become ready in time"
        exit 1
        """.trimIndent()
    )
}

tasks.register<Exec>("composeDown") {
    description = "Stop test environment"
    commandLine(
        "sh", "-c",
        "docker compose -f infra/docker-compose.yml down; " +
        "docker compose -f infra/docker-compose-infra.yml down"
    )
}
