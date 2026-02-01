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
    // Awaitility for polling/retry
    testImplementation("org.awaitility:awaitility:4.2.0")
}

tasks.test {
    useJUnitPlatform()
    // Cucumber 태그 필터 전달
    systemProperty("cucumber.filter.tags", System.getProperty("cucumber.filter.tags") ?: "")
    testLogging {
        showStandardStreams = true
    }
}

// Infra (DB 등)
tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (DB, etc.)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml up -d")
}

tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml down -v")
}

// Services (kiosk, admin, reservation)
tasks.register<Exec>("servicesUp") {
    group = "infra"
    description = "Start services via docker compose (build + up)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml up -d --build")
}

tasks.register<Exec>("servicesDown") {
    group = "infra"
    description = "Stop services and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml down -v")
}

// All (infra + services)
tasks.register("allUp") {
    group = "infra"
    description = "Start all (infra + services)"
    dependsOn("infraUp", "servicesUp")
}

tasks.register("allDown") {
    group = "infra"
    description = "Stop all (infra + services)"
    dependsOn("infraDown", "servicesDown")
}
