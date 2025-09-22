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

tasks.register<Exec>("servicesUp") {
    group = "infra"
    description = "Run all services (admin, reservation, kiosk) via docker compose (build + up)"
    commandLine(
        "/opt/homebrew/bin/docker", "compose",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("servicesDown") {
    group = "infra"
    description = "Stop all services compose and remove volumes"
    commandLine(
        "/opt/homebrew/bin/docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

tasks.register("syncRepos") {
    group = "repos"
    description = "Clone or update all repositories (admin, reservation, kiosk)"

    doLast {
        file("repos").mkdirs()

        val repositories = mapOf(
            "atdd-camping-admin" to "https://github.com/donggi-lee-bit/atdd-camping-admin.git",
            "atdd-camping-reservation" to "https://github.com/donggi-lee-bit/atdd-camping-reservation.git",
            "atdd-camping-kiosk" to "https://github.com/donggi-lee-bit/atdd-camping-kiosk.git"
        )

        repositories.forEach { (repoName, repoUrl) ->
            val repoDir = file("repos/$repoName")
            val gitDir = file("repos/$repoName/.git")


            // 저장소가 존재하지 않으면 clone
            if (!gitDir.exists()) {
                ProcessBuilder("git", "clone", repoUrl, repoDir.path)
                    .inheritIO()
                    .start()
                    .waitFor()
            } else {
                // 존재하면 update
                ProcessBuilder("git", "pull", "origin", "donggi-lee-bit")
                    .directory(repoDir)
                    .inheritIO()
                    .start()
                    .waitFor()
            }
        }
    }
}

tasks.register<Delete>("cleanRepos") {
    group = "repos"
    description = "Clean the repos directory"
    delete("repos")
}
