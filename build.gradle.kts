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
val adminBaseUrl: String = System.getenv("ADMIN_BASE_URL") ?: "http://localhost:18081"
val reservationBaseUrl: String = System.getenv("RESERVATION_BASE_URL") ?: "http://localhost:18082"

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
    testImplementation("io.cucumber:cucumber-picocontainer:7.14.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.test {
    useJUnitPlatform()
    dependsOn("composeUp")
    finalizedBy("composeDown")
    systemProperty("kiosk.base.url", kioskBaseUrl)
    systemProperty("admin.base.url", adminBaseUrl)
    systemProperty("reservation.base.url", reservationBaseUrl)
}

// ========== Docker Compose Tasks ==========

tasks.register<Exec>("composeUp") {
    description = "Start test environment"
    commandLine(
        "sh", "-c",
        "docker compose -f infra/docker-compose-infra.yml -f infra/docker-compose.yml up -d --build"
    )
}

tasks.register<Exec>("composeDown") {
    description = "Stop test environment"
    commandLine(
        "sh", "-c",
        "docker compose -f infra/docker-compose-infra.yml -f infra/docker-compose.yml down"
    )
}
