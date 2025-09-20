import org.gradle.api.tasks.Exec

// Default compose file path used by docker-related tasks
val defaultComposeFile = project.rootProject.layout.projectDirectory
    .file("infra/docker-compose.yml").asFile.absolutePath

// Application up (including image build)
tasks.register<Exec>("appUp") {
    group = "docker"
    description = "Run 'docker compose up -d --build' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "up", "-d", "--build"
    )
}

// Application down
tasks.register<Exec>("appDown") {
    group = "docker"
    description = "Run 'docker compose down' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "down"
    )
}

// Status check (ps)
tasks.register<Exec>("ps") {
    group = "docker"
    description = "Run 'docker compose ps' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", defaultComposeFile,
        "ps"
    )
}


// Kiosk Logs
tasks.register<Exec>("kioskLogs") {
    group = "docker"
    description = "Show last 100 lines of kiosk container logs. Default container: 'atdd-kiosk'"
    commandLine(
        "docker", "logs",
        "atdd-kiosk", "--tail", "100"
    )
}

// Clone or update the kiosk repository
tasks.register<Exec>("cloneKioskRepo") {
    description = "Clone or update https://github.com/mdy0501/atdd-camping-kiosk under repo/ at project root."
    group = "setup"

    val repoDir = project.file("repo/atdd-camping-kiosk")

    doFirst {
        repoDir.parentFile.mkdirs()
        if (repoDir.exists()) {
            workingDir(repoDir)
            commandLine("git", "pull")
        } else {
            workingDir(repoDir.parentFile)
            commandLine(
                "git", "clone",
                "--branch", "main",
                "https://github.com/mdy0501/atdd-camping-kiosk"
            )
        }
    }
}

// Admin Logs
tasks.register<Exec>("adminLogs") {
    group = "docker"
    description = "Show last 100 lines of admin container logs. Default container: 'atdd-admin'"
    commandLine(
        "docker", "logs",
        "atdd-admin", "--tail", "100"
    )
}

// Clone or update the admin repository
tasks.register<Exec>("cloneAdminRepo") {
    description = "Clone or update https://github.com/mdy0501/atdd-camping-admin under repo/ at project root."
    group = "setup"

    val repoDir = project.file("repo/atdd-camping-admin")

    doFirst {
        repoDir.parentFile.mkdirs()
        if (repoDir.exists()) {
            workingDir(repoDir)
            commandLine("git", "pull")
        } else {
            workingDir(repoDir.parentFile)
            commandLine(
                "git", "clone",
                "--branch", "main",
                "https://github.com/mdy0501/atdd-camping-admin"
            )
        }
    }
}

// Reservation Logs
tasks.register<Exec>("reservationLogs") {
    group = "docker"
    description = "Show last 100 lines of reservation container logs. Default container: 'atdd-reservation'"
    commandLine(
        "docker", "logs",
        "atdd-reservation", "--tail", "100"
    )
}

// Clone or update the reservation repository
tasks.register<Exec>("cloneReservationRepo") {
    description = "Clone or update https://github.com/mdy0501/atdd-camping-reservation under repo/ at project root."
    group = "setup"

    val repoDir = project.file("repo/atdd-camping-reservation")

    doFirst {
        repoDir.parentFile.mkdirs()
        if (repoDir.exists()) {
            workingDir(repoDir)
            commandLine("git", "pull")
        } else {
            workingDir(repoDir.parentFile)
            commandLine(
                "git", "clone",
                "--branch", "main",
                "https://github.com/mdy0501/atdd-camping-reservation"
            )
        }
    }
}