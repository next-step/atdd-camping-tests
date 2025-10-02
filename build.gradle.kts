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
    testImplementation("org.awaitility:awaitility:4.2.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.test {
    useJUnitPlatform()
}

// Kiosk Infrastructure Tasks
tasks.register<Exec>("kioskCheckDocker") {
    group = "infra"
    description = "Check Docker daemon is running"
    commandLine("docker", "info")
}

tasks.register<Exec>("kioskCloneNew") {
    group = "infra"
    description = "Clone kiosk repository (if not exists)"

    onlyIf {
        !file("repos/atdd-camping-kiosk/.git").exists()
    }

    commandLine(
        "git", "clone",
        "--branch", "main",
        "--single-branch",
        "--depth", "1",
        "https://github.com/next-step/atdd-camping-kiosk",
        "repos/atdd-camping-kiosk"
    )
}

tasks.register<Exec>("kioskFetch") {
    group = "infra"
    description = "Fetch kiosk repository updates"

    onlyIf {
        file("repos/atdd-camping-kiosk/.git").exists()
    }

    workingDir = file("repos/atdd-camping-kiosk")
    commandLine("git", "fetch", "origin", "main")
}

tasks.register<Exec>("kioskSwitch") {
    group = "infra"
    description = "Switch to main branch"
    dependsOn("kioskFetch")

    onlyIf {
        file("repos/atdd-camping-kiosk/.git").exists()
    }

    workingDir = file("repos/atdd-camping-kiosk")
    commandLine("git", "switch", "main")
}

tasks.register<Exec>("kioskPull") {
    group = "infra"
    description = "Pull latest changes"
    dependsOn("kioskSwitch")

    onlyIf {
        file("repos/atdd-camping-kiosk/.git").exists()
    }

    workingDir = file("repos/atdd-camping-kiosk")
    commandLine("git", "pull", "--rebase")
}

tasks.register("kioskClone") {
    group = "infra"
    description = "Clone or update kiosk repository"
    dependsOn("kioskCloneNew", "kioskPull")

    doLast {
        println("[OK] kiosk repo synced.")
    }
}

tasks.register<Exec>("kioskBuild") {
    group = "infra"
    description = "Build kiosk JAR"
    dependsOn("kioskClone")

    doFirst {
        val gradlewPath = file("repos/atdd-camping-kiosk/gradlew")
        if (gradlewPath.exists()) {
            gradlewPath.setExecutable(true)
        }
    }

    workingDir = file("repos/atdd-camping-kiosk")
    commandLine("./gradlew", "clean", "build", "-x", "test", "--warning-mode", "all")
}

tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Start kiosk via docker compose"
    dependsOn("kioskBuild")

    workingDir = projectDir
    commandLine(
        "docker", "compose",
        "-p", "atdd-infra",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build", "--remove-orphans"
    )
}

tasks.register<Exec>("kioskStatus") {
    group = "infra"
    description = "Show kiosk container status"
    commandLine(
        "docker", "compose",
        "-p", "atdd-infra",
        "-f", "infra/docker-compose.yml",
        "ps"
    )
}

tasks.register<Exec>("kioskLogs") {
    group = "infra"
    description = "Show kiosk logs"
    commandLine(
        "docker", "compose",
        "-p", "atdd-infra",
        "-f", "infra/docker-compose.yml",
        "logs", "kiosk", "--tail=100"
    )
    isIgnoreExitValue = true
}

tasks.register("kioskUp") {
    group = "infra"
    description = "Clone, build, and start kiosk service via docker compose"
    dependsOn("kioskCheckDocker", "kioskComposeUp", "kioskStatus", "kioskLogs")

    doLast {
        println("[OK] kiosk up.")
    }
}

tasks.register<Exec>("kioskDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )

    doLast {
        println("[OK] kiosk down.")
    }
}

tasks.register<Test>("testSmoke") {
    group = "verification"
    description = "Run smoke tests (Cucumber features)"
    useJUnitPlatform()

    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    filter {
        includeTestsMatching("com.camping.tests.RunCucumberTest")
    }

    doFirst {
        println("[INFO] Running smoke tests...")
    }

    doLast {
        println("[OK] Smoke tests completed.")
    }
}
