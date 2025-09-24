import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.DriverManager
import kotlin.system.exitProcess

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.mysql:mysql-connector-j:9.4.0")
    }
}

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
    waitForMySql(maxAttempts = 30)
    waitForWireMock(maxAttempts = 30)
}

fun waitForMySql(maxAttempts: Int) {
    Class.forName("com.mysql.cj.jdbc.Driver")
    var attempts = 1

    while (attempts < maxAttempts) {
        try {
            val mysqlUrl = "jdbc:mysql://localhost:3306/atdd"
            val username = "root"
            val password = "secret"

            DriverManager.getConnection(mysqlUrl, username, password).use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT 1").use { resultSet ->
                        if (resultSet.next()) {
                            println("MySQL is ready!")
                            return
                        } else {
                            throw RuntimeException("No result returned from SELECT 1 query")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            attempts++
            println("MySQL is not ready yet (attempt $attempts/$maxAttempts): ${e.message}")
            Thread.sleep(1_000)
        }
    }

    println("MySQL did not become ready in time. Exiting.")
    exitProcess(1)
}

fun waitForWireMock(maxAttempts: Int) {
    val client = HttpClient.newHttpClient()
    var attempts = 1

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
            Thread.sleep(1_000)
        }
    }

    println("Wiremock did not become ready in time. Exiting.")
    exitProcess(1)
}
