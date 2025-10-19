import java.io.ByteArrayOutputStream
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations


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
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.test {
    useJUnitPlatform()
}

data class Repository(
    val name: String,
    val url: String,
    val branch: String,
)

val repositoriesToClone = listOf(
    Repository("kiosk", "https://github.com/MoonJeWoong/atdd-camping-kiosk", "main"),
    Repository("admin", "https://github.com/MoonJeWoong/atdd-camping-admin", "main"),
    Repository("reservation", "https://github.com/MoonJeWoong/atdd-camping-reservation", "main")
)

tasks.register("cloneRepositories") {
    group = "setup"
    description = "Clones or pulls all repositories."

    doLast {
        repositoriesToClone.forEach { repository ->
            val repoDir = file("repos/${repository.name}")
            if (!repoDir.exists()) {
                logger.info("Cloning ${repository.name} repository from ${repository.url}")
                repoDir.parentFile.mkdirs()
                project.exec {
                    commandLine("git", "clone", "--branch", repository.branch, repository.url, repoDir)
                }
            } else {
                logger.info("${repository.name} repository already exists. Pulling latest changes.")
                project.exec {
                    workingDir = repoDir
                    commandLine("git", "pull")
                }
            }
        }
    }
}

tasks.register<Exec>("dockerComposeUp") {
    group = "docker"
    description = "Starts the services using docker-compose."
    dependsOn("waitForDbContainer")
    mustRunAfter("waitForDbContainer")
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

tasks.register<WaitForDbTask>("waitForDbContainer") {
    group = "docker"
    description = "Waits for the DB container to be healthy with a timeout."
    dependsOn("startInfraContainer")
    mustRunAfter("startInfraContainer")
}

abstract class WaitForDbTask : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun exec() {

        val maxRetries = 20
        var isReady = false

        logger.lifecycle("Waiting for DB container to be ready for queries (max 20 seconds)...")

        for (i in 1..maxRetries) {
            // Suppress standard output to keep logs clean, but show errors.
            val standardOutput = ByteArrayOutputStream()

            val result = execOperations.exec {
                commandLine("sh", "-c", "docker exec atdd-db mysql -uroot -psecret -e 'SELECT 1'")
                isIgnoreExitValue = true
                this.standardOutput = standardOutput
                errorOutput = System.err
            }

            if (result.exitValue == 0) {
                isReady = true
                logger.lifecycle("DB container is ready for queries.")
                break
            } else {
                logger.lifecycle("Waiting for DB... (Attempt ${i}/${maxRetries})")
                // Use a longer sleep interval to give the DB more time to initialize.
                Thread.sleep(1000)
            }
        }

        if (!isReady) {
            throw GradleException("DB container 'atdd-db' did not become ready for queries within 100 seconds.")
        }
    }
}

tasks.register<Exec>("stopInfraContainers") {
    group = "docker"
    description = "Stops the services from docker-compose-infra.yml."
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml down")
}

tasks.register("setupForAcceptanceTest") {
    group = "setup"
    description = "Runs all setup tasks in order: cloneRepositories, startInfraContainer, waitForDbContainer, dockerComposeUp"
    dependsOn(tasks.named("cloneRepositories"), tasks.named("dockerComposeUp"))
}
