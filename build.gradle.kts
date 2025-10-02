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
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

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
val kioskRepoDir = file("repos/atdd-camping-kiosk")
val adminRepoDir = file("repos/atdd-camping-admin")
val reservationRepoDir = file("repos/atdd-camping-reservation")
val composeProject = "atdd-infra"
val composeFile = "infra/docker-compose.yml"

fun dockerCompose(vararg args: String) = listOf("docker", "compose", "-p", composeProject, "-f", composeFile) + args

fun createCloneTasks(
    prefix: String,
    repoDir: File,
    repoUrl: String,
) {
    tasks.register<Exec>("${prefix}CloneNew") {
        group = "infra"
        description = "Clone $prefix repository (if not exists)"
        onlyIf { !repoDir.resolve(".git").exists() }
        commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1", repoUrl, repoDir.absolutePath)
    }

    tasks.register<Exec>("${prefix}Fetch") {
        group = "infra"
        description = "Fetch $prefix repository updates"
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "fetch", "origin", "main")
    }

    tasks.register<Exec>("${prefix}Switch") {
        group = "infra"
        description = "Switch to main branch"
        dependsOn("${prefix}Fetch")
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "switch", "main")
    }

    tasks.register<Exec>("${prefix}Pull") {
        group = "infra"
        description = "Pull latest changes"
        dependsOn("${prefix}Switch")
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "pull", "--rebase")
    }

    tasks.register("${prefix}Clone") {
        group = "infra"
        description = "Clone or update $prefix repository"
        dependsOn("${prefix}CloneNew", "${prefix}Pull")
        doLast { println("[OK] $prefix repo synced.") }
    }
}

createCloneTasks("kiosk", kioskRepoDir, "https://github.com/next-step/atdd-camping-kiosk")
createCloneTasks("admin", adminRepoDir, "https://github.com/next-step/atdd-camping-admin")
createCloneTasks("reservation", reservationRepoDir, "https://github.com/next-step/atdd-camping-reservation")

tasks.register<Exec>("kioskCheckDocker") {
    group = "infra"
    description = "Check Docker daemon is running"
    commandLine("docker", "info")
}

tasks.register("cloneAll") {
    group = "infra"
    description = "Clone or update all repositories (admin, reservation, kiosk)"
    dependsOn("adminClone", "reservationClone", "kioskClone")
}

tasks.register<Exec>("kioskBuild") {
    group = "infra"
    description = "Build kiosk JAR"
    dependsOn("kioskClone")
    doFirst { kioskRepoDir.resolve("gradlew").takeIf { it.exists() }?.setExecutable(true) }
    workingDir = kioskRepoDir
    commandLine("./gradlew", "clean", "build", "-x", "test", "--warning-mode", "all")
}

tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Start kiosk via docker compose"
    dependsOn("kioskBuild")
    workingDir = projectDir
    commandLine(dockerCompose("up", "-d", "--build", "--remove-orphans"))
}

tasks.register<Exec>("kioskStatus") {
    group = "infra"
    description = "Show kiosk container status"
    commandLine(dockerCompose("ps"))
}

tasks.register<Exec>("kioskLogs") {
    group = "infra"
    description = "Show kiosk logs"
    commandLine(dockerCompose("logs", "kiosk", "--tail=100"))
    isIgnoreExitValue = true
}

tasks.register("kioskUp") {
    group = "infra"
    description = "Clone, build, and start kiosk service via docker compose"
    dependsOn("kioskCheckDocker", "kioskComposeUp", "kioskStatus", "kioskLogs")
    doLast { println("[OK] kiosk up.") }
}

tasks.register<Exec>("kioskDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(dockerCompose("down", "-v"))
    doLast { println("[OK] kiosk down.") }
}

tasks.register<Test>("testSmoke") {
    group = "verification"
    description = "Run smoke tests"
    useJUnitPlatform()
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }
    doFirst { println("[INFO] Running smoke tests...") }
    doLast { println("[OK] Smoke tests completed.") }
}
