import org.gradle.api.GradleException
import java.net.HttpURLConnection
import java.net.URL

// =================================================================
// Docker Compose Tasks
// =================================================================

// --- Infra Tasks (docker-compose-infra.yml) ---
tasks.register<Exec>("InfraComposeUp") {
    group = "docker"
    description = "Run infra services via docker compose"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "up", "-d"
    )
}

tasks.register<Exec>("InfraComposeDown") {
    group = "docker"
    description = "Stop infra services and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "down", "-v"
    )
    dependsOn("ServiceComposeDown") // 서비스가 먼저 내려가야 함
}

// --- Service Tasks (docker-compose.yml) ---
tasks.register<Exec>("ServiceComposeUp") {
    group = "docker"
    description = "Run application services via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
    dependsOn("InfraComposeUp") // 인프라가 먼저 실행되어야 함
}

tasks.register<Exec>("ServiceComposeDown") {
    group = "docker"
    description = "Stop application services and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

// --- Helper Tasks ---
tasks.register<Exec>("ServicePs") {
    group = "docker"
    description = "Show application service container status"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "ps"
    )
}

tasks.register<Exec>("ServiceLog") {
    group = "docker"
    description = "Show logs for a specific service container"
    commandLine(
        "docker", "logs", "atdd-kiosk",
        "--tail", "100", "-f"
    )
}

// --- Master Tasks ---
tasks.register("composeUp") {
    group = "docker"
    description = "Bring up all infra and application services in the correct order."
    dependsOn("ServiceComposeUp")
}

tasks.register("composeDown") {
    group = "docker"
    description = "Bring down all application and infra services in the correct order."
    dependsOn("InfraComposeDown")
}

// =================================================================
// Automation Tasks
// =================================================================

tasks.register("gitPullAll") {
    group = "automation"
    description = "Run git pull on all sub-projects in the repos directory."
    doLast {
        val reposDir = file("repos")
        if (reposDir.exists() && reposDir.isDirectory) {
            reposDir.listFiles()?.forEach { repo ->
                if (repo.isDirectory) {
                    println("Pulling latest changes for ${repo.name}...")
                    exec {
                        workingDir = repo
                        commandLine("git", "pull")
                    }
                }
            }
        }
    }
}

tasks.register("waitForServices") {
    group = "automation"
    description = "Waits for all application services to be healthy."
    dependsOn("composeUp")
    doLast {
        val services = listOf(
            "kiosk" to "http://localhost:8081/health",
            "admin" to "http://localhost:8082/login",
            "reservation" to "http://localhost:8083/",
            "payment-mock" to "http://localhost:8084/__admin/mappings"
        )
        val retries = 30
        val delay = 2000L // 2 seconds

        println("Waiting for services to become healthy...")

        for (i in 1..retries) {
            val healthyServices = services.filter { (name, url) ->
                try {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 1000
                    connection.readTimeout = 1000
                    val responseCode = connection.responseCode
                    if (responseCode == 200) {
                        println("✅ Service '$name' is healthy.")
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    false
                }
            }

            if (healthyServices.size == services.size) {
                println("🎉 All services are healthy!")
                return@doLast
            }

            print(".")
            Thread.sleep(delay)
        }

        throw GradleException("❌ Timeout: Not all services became healthy within the specified time.")
    }
}


tasks.register("acceptanceTest") {
    group = "automation"
    description = "Run the full acceptance test suite: git pull, docker up, test, docker down."
    dependsOn("gitPullAll")
    dependsOn("test")
}
