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

    // AssertJ
    testImplementation("org.assertj:assertj-core:3.24.2")
}


tasks.test {
    useJUnitPlatform()
    dependsOn("applicationComposeUp")
    finalizedBy("applicationComposeDown")
}

tasks.register<Exec>("dbComposeUp") {
    group = "infra"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "up", "-d", "--build", "--wait")
}

tasks.register<Exec>("dbComposeDown") {
    group = "infra"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "down", "-v")
}

tasks.register<Exec>("applicationComposeUp") {
    group = "infra"

    doFirst {
        println("🧹 cleaning docker compose")
    }

    dependsOn("dbComposeUp")
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "up", "-d", "--build", "--wait")
}

tasks.register<Exec>("applicationComposeDown") {
    group = "infra"
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "down", "-v")
    finalizedBy("dbComposeDown")
}

tasks.register<Exec>("composeCleanup") {
    group = "infra"
    description = "Always cleanup docker compose"

    isIgnoreExitValue = true

    commandLine(
        "sh", "-c",
        """
        docker compose -f infra/docker-compose.yml down -v || true
        docker compose -f infra/docker-compose-infra.yml down -v || true
        """.trimIndent()
    )
}

tasks.named("check") {
    finalizedBy("composeCleanup")
}
