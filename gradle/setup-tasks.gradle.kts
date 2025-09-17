tasks.register("setupTestInfra") {
    group = "setup"

    createReposDirectory()
    cloneKioskRepository()
    dockerComposeUp()
}

fun createReposDirectory() {
    val reposDir = file("repos")

    if (reposDir.exists().not()) {
        reposDir.mkdirs()
        println("Created 'repos' directory")
    } else {
        println("'repos' directory already exists")
    }
}

fun cloneKioskRepository() {
    val kioskRepoDir = file("repos/atdd-camping-kiosk")

    if (!kioskRepoDir.exists()) {
        exec {
            commandLine(
                "git", "clone",
                "--depth", "1",
                "--single-branch",
                "--branch", "main",
                "https://github.com/next-step/atdd-camping-kiosk.git",
                "repos/atdd-camping-kiosk",
            )
        }
        println("Successfully cloned atdd-camping-kiosk repository")
    } else {
        println("atdd-camping-kiosk repository already exists")
    }
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
