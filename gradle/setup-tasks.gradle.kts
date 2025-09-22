tasks.register("setupTestInfra") {
    group = "setup"

    createReposDirectory()
    listOf(
        Repository(name = "atdd-camping-kiosk", url = "https://github.com/next-step/atdd-camping-kiosk", branch = "main"),
        Repository(name = "atdd-camping-admin", url = "https://github.com/ivvve/atdd-camping-admin", branch = "mysql"),
        Repository(name = "atdd-camping-reservation", url = "https://github.com/ivvve/atdd-camping-reservation", branch = "mysql"),
    ).forEach { setupRepository(it) }

    runInfraContainers()
    Thread.sleep(5000) // Wait for infra containers to be fully up
    runServiceContainers()
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
