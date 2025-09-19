tasks.register("setupTestInfra") {
    group = "setup"

    createReposDirectory()
    setupRepository("atdd-camping-kiosk")
    setupRepository("atdd-camping-admin")
    setupRepository("atdd-camping-reservation")
    dockerComposeUp()
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

fun setupRepository(repositoryName: String) {
    val repositoryDir = file("repos/${repositoryName}")

    if (repositoryDir.exists()) {
        println("${repositoryName} repository already exists, pulling latest changes...")
        exec {
            workingDir = repositoryDir
            commandLine("git", "pull", "origin", "main")
        }
        println("Successfully updated ${repositoryName} repository")
        return
    }

    exec {
        commandLine(
            "git", "clone",
            "--depth", "1",
            "--single-branch",
            "--branch", "main",
            "https://github.com/next-step/${repositoryName}.git",
            "repos/${repositoryName}",
        )
    }
    println("Successfully cloned ${repositoryName} repository")
}

fun dockerComposeUp() {
    exec {
        commandLine(
            "docker-compose",
            "-f", "./infra/docker-compose.yml",
            "up", "-d",
        )
    }
    println("Docker containers are up and running")
}
