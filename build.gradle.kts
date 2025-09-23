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

tasks.register<Exec>("cloneRepo") {
    group = "setup"
    description = "Clone the camping kiosk repository"

    doFirst {
        val reposDir = file("repos")
        if (!reposDir.exists()) {
            reposDir.mkdirs()
        }

        val targetDir = file("repos/atdd-camping-kiosk")
        if (targetDir.exists()) {
            delete(targetDir)
        }
    }

    commandLine("git", "clone", "--depth", "1", "--branch", "main", "--single-branch", "https://github.com/next-step/atdd-camping-kiosk", "repos/atdd-camping-kiosk")
}

tasks.register<Exec>("dockerUp") {
    group = "docker"
    description = "Start camping kiosk application with docker-compose"

    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "up", "--build", "-d")
}

tasks.register<Exec>("dockerDown") {
    group = "docker"
    description = "Stop camping kiosk application with docker-compose"

    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "down")
}
