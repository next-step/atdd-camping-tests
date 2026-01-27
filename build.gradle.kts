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
}

tasks.test {
    useJUnitPlatform()
}

// === Infra Tasks ===
tasks.register<Exec>("composeUp") {
    group = "infra"
    description = "Start kiosk service via docker compose"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "up", "-d", "--build")
}

tasks.register<Exec>("composeDown") {
    group = "infra"
    description = "Stop kiosk service and remove volumes"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "down", "-v")
}

tasks.register<Exec>("composeLogs") {
    group = "infra"
    description = "Show kiosk service logs"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "logs", "-f", "kiosk")
}

tasks.register<Exec>("composePs") {
    group = "infra"
    description = "Show running containers status"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "ps")
}

tasks.register<Test>("smokeTest") {
    group = "verification"
    description = "Run smoke tests only"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@smoke")
}