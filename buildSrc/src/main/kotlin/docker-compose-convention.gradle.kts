// Docker Compose 태스크
tasks.register<Exec>("composeUp") {
    group = "docker"
    description = "Start all services"
    workingDir = file("infra")

    doFirst {
        val kioskRepoDir = file("infra/repos/atdd-camping-kiosk")
        if (!kioskRepoDir.exists()) {
            file("infra/repos").mkdirs()
            project.exec {
                commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1",
                          "git@github.com:next-step/atdd-camping-kiosk.git", "infra/repos/atdd-camping-kiosk")
            }
        }

        val adminRepoDir = file("infra/repos/atdd-camping-admin")
        if (!adminRepoDir.exists()) {
            file("infra/repos").mkdirs()
            project.exec {
                commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1",
                          "git@github.com:suzhanlee/atdd-camping-admin.git", "infra/repos/atdd-camping-admin")
            }
        }

        val reservationRepoDir = file("infra/repos/atdd-camping-reservation")
        if (!reservationRepoDir.exists()) {
            file("infra/repos").mkdirs()
            project.exec {
                commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1",
                          "git@github.com:suzhanlee/atdd-camping-reservation.git", "infra/repos/atdd-camping-reservation")
            }
        }
    }

    commandLine("docker", "compose", "up", "-d")
}

tasks.register<Exec>("composeDown") {
    group = "docker"
    description = "Stop all services"
    workingDir = file("infra")
    commandLine("docker", "compose", "down")
}

tasks.register("smokeTest") {
    group = "verification"
    description = "Run smoke tests"
    dependsOn("composeUp", "test")
    finalizedBy("composeDown")

    doLast {
        println("Smoke test completed for all services:")
        println("- Kiosk: http://localhost:18081")
        println("- Admin: http://localhost:18082")
        println("- Reservation: http://localhost:18083")
    }
}