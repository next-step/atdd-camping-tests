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

val defaultComposeFile = project.rootProject.layout.projectDirectory.file("infra/docker-compose.yml").asFile.absolutePath

// Application up (including image build)
tasks.register<Exec>("kioskAppUp") {
    group = "docker"
    description = "Run 'docker compose up -d --build' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "up", "-d", "--build"
    )
}

// Application down
tasks.register<Exec>("kioskAppDown") {
    group = "docker"
    description = "Run 'docker compose down' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "down"
    )
}

// Status check (ps)
tasks.register<Exec>("ps") {
    group = "docker"
    description = "Run 'docker compose ps' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "ps"
    )
}

// Logs (kiosk only, tail 100)
tasks.register<Exec>("logs") {
    group = "docker"
    description = "Show last 100 lines of kiosk container logs. Default container: 'atdd-kiosk'. Override with -PkioskContainerName=..."
    commandLine(
        "docker", "logs",
        "atdd-kiosk", "--tail", "100"
    )
}

tasks.register<Exec>("cloneKioskRepo") {
    description = "Clone https://github.com/next-step/atdd-camping-kiosk into repo/ at project root."
    group = "setup"

    val repoDir = project.file("repo/kiosk")

    onlyIf { !repoDir.exists() }

    doFirst { repoDir.parentFile.mkdirs() }

    workingDir(repoDir.parentFile)
    commandLine(
        "git", "clone",
        "--branch", "main",
        "https://github.com/next-step/atdd-camping-kiosk"
    )
//    doLast {
//        val repoDir = project.rootProject.layout.projectDirectory.dir("repo").asFile
//        println("[repoDir]: $repoDir")
//        val kioskRepoDir = repoDir.resolve("atdd-camping-kiosk")
//        println("[kioskRepoDir]: $kioskRepoDir")
//
//        if (kioskRepoDir.exists()) {
//            println("atdd-camping-kiosk repository already exists in repo/, skipping clone.")
//        } else {
//            if (!repoDir.exists()) {
//                repoDir.mkdirs()
//            }
//            project.exec {
//                workingDir = repoDir
//                commandLine(
//                    "git", "clone",
//                    "--branch", "main",
//                    "--single-branch",
//                    "https://github.com/next-step/atdd-camping-kiosk"
//                )
//            }
//        }
//    }
}
