tasks.register("setupTestInfra") {
    group = "setup"

    createReposDirectory()
    setupKioskRepository()
    setupAdminRepository()
    setupReservationRepository()
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

fun setupKioskRepository() {
    val kioskRepoDir = file("repos/atdd-camping-kiosk")

    if (kioskRepoDir.exists()) {
        println("atdd-camping-kiosk repository already exists, pulling latest changes...")
        exec {
            workingDir = kioskRepoDir
            commandLine("git", "pull", "origin", "main")
        }
        println("Successfully updated atdd-camping-kiosk repository")
        return
    }

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
}

fun setupAdminRepository() {
    val adminRepoDir = file("repos/atdd-camping-admin")

    if (adminRepoDir.exists()) {
        println("atdd-camping-admin repository already exists, pulling latest changes...")
        exec {
            workingDir = adminRepoDir
            commandLine("git", "pull", "origin", "main")
        }
        println("Successfully updated atdd-camping-admin repository")
        return
    }

    exec {
        commandLine(
            "git", "clone",
            "--depth", "1",
            "--single-branch",
            "--branch", "main",
            "https://github.com/next-step/atdd-camping-admin.git",
            "repos/atdd-camping-admin",
        )
    }
    println("Successfully cloned atdd-camping-admin repository")
}

fun setupReservationRepository() {
    val reservationRepoDir = file("repos/atdd-camping-reservation")

    if (reservationRepoDir.exists()) {
        println("atdd-camping-reservation repository already exists, pulling latest changes...")
        exec {
            workingDir = reservationRepoDir
            commandLine("git", "pull", "origin", "main")
        }
        println("Successfully updated atdd-camping-reservation repository")
        return
    }

    exec {
        commandLine(
            "git", "clone",
            "--depth", "1",
            "--single-branch",
            "--branch", "main",
            "https://github.com/next-step/atdd-camping-reservation.git",
            "repos/atdd-camping-reservation",
        )
    }
    println("Successfully cloned atdd-camping-reservation repository")
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
