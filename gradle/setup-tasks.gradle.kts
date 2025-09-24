import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.system.exitProcess

tasks.register("setupTestInfra") {
    group = "setup"

    doLast {
        createReposDirectory()
        listOf(
            Repository(
                name = "atdd-camping-kiosk",
                url = "https://github.com/next-step/atdd-camping-kiosk",
                branch = "main"
            ),
            Repository(
                name = "atdd-camping-admin",
                url = "https://github.com/ivvve/atdd-camping-admin",
                branch = "mysql"
            ),
            Repository(
                name = "atdd-camping-reservation",
                url = "https://github.com/ivvve/atdd-camping-reservation",
                branch = "mysql"
            ),
        ).forEach { setupRepository(it) }

        runInfraContainers()
        waitForInfra()
        runServiceContainers()
    }
}

fun createReposDirectory() {
    val reposDir = file("repos")

    if (reposDir.exists()) {
        println("'repos' directory already exists")
        return
    }

    reposDir.mkdirs()
    println("Created 'repos' directory")
}

data class Repository(
    val name: String,
    val url: String,
    val branch: String,
)

fun setupRepository(repository: Repository) {
    val repositoryDir = file("repos/${repository.name}")

    if (repositoryDir.exists()) {
        println("${repository.name} repository already exists, pulling latest changes...")
        exec {
            workingDir = repositoryDir
            commandLine("git", "pull", "origin", repository.branch)
        }
        println("Successfully updated ${repository.name} repository")
        return
    }

    exec {
        commandLine(
            "git", "clone",
            "--depth", "1",
            "--single-branch",
            "--branch", repository.branch,
            repository.url,
            "repos/${repository.name}",
        )
    }
    println("Successfully cloned ${repository.name} repository")
}

fun runInfraContainers() {
    exec {
        commandLine(
            "docker-compose",
            "-f", "./infra/docker-compose-infra.yml",
            "up", "-d",
        )
    }
}

fun runServiceContainers() {
    exec {
        commandLine(
            "docker-compose",
            "-f", "./infra/docker-compose.yml",
            "up", "-d",
        )
    }
    println("Docker containers are up and running")
}

fun waitForInfra() {
    waitForMySql()
    waitForWireMock()
}

fun waitForMySql() {
    Thread.sleep(5_000L)
}

fun waitForWireMock() {
    val maxAttempts = 30
    var attempts = 0
    val client = HttpClient.newHttpClient()

    while (attempts < maxAttempts) {
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8090/__admin/mappings"))
                .timeout(java.time.Duration.ofSeconds(5))
                .GET()
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() == 200) {
                println("WireMock is ready!")
                return
            }
        } catch (e: Exception) {
            attempts++
            println("Wiremock is not ready yet (attempt $attempts/$maxAttempts): ${e.message}")
            if (attempts < maxAttempts) {
                Thread.sleep(1000)
            }
        }
    }

    println("Wiremock did not become ready in time. Exiting.")
    exitProcess(1)
}
