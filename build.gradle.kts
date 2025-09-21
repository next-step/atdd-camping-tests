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

// kiosk 저장소를 repos 하위 경로에 clone 한다
// url은 https://github.com/MoonJeWoong/atdd-camping-kiosk 를 사용한다
// clone 시 기본적으로 main 브랜치를 사용하도록 한다
tasks.register<Exec>("cloneKioskRepository") {
    group = "setup"
    description = "Clones the kiosk repository."

    val repoDir = file("repos/kiosk")

    onlyIf { !repoDir.exists() }

    doFirst {
        repoDir.parentFile.mkdirs()
    }

    commandLine("git", "clone", "--branch", "main", "https://github.com/MoonJeWoong/atdd-camping-kiosk", repoDir)
}

tasks.register<Exec>("cloneAdminRepository") {
    group = "setup"
    description = "Clones the admin repository."

    val repoDir = file("repos/admin")

    onlyIf { !repoDir.exists() }

    doFirst {
        repoDir.parentFile.mkdirs()
    }

    commandLine("git", "clone", "--branch", "main", "https://github.com/MoonJeWoong/atdd-camping-admin", repoDir)
}

tasks.register<Exec>("cloneReservationRepository") {
    group = "setup"
    description = "Clones the reservation repository."

    val repoDir = file("repos/reservation")

    onlyIf { !repoDir.exists() }

    doFirst {
        repoDir.parentFile.mkdirs()
    }

    commandLine("git", "clone", "--branch", "main", "https://github.com/MoonJeWoong/atdd-camping-reservation", repoDir)
}

tasks.register<Exec>("dockerComposeUp") {
    group = "docker"
    description = "Starts the services using docker-compose."
    dependsOn("startInfraContainer")
    mustRunAfter("startInfraContainer")
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml up -d")
}

tasks.register<Exec>("dockerComposeDown") {
    group = "docker"
    description = "Stops the services using docker-compose."
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml down")
}

tasks.register<Exec>("startInfraContainer") {
    group = "docker"
    description = "Starts only the infra container using docker-compose-infra.yml."
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml up -d db")
}

tasks.register<Exec>("stopInfraContainers") {
    group = "docker"
    description = "Stops the services from docker-compose-infra.yml."
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml down")
}
