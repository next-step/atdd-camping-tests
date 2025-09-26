fun cloneRepoIfNotExists(repoName: String, repoUrl: String) {
    val repoDir = file("infra/repos/$repoName")
    if (!repoDir.exists()) {
        file("infra/repos").mkdirs()
        project.exec {
            commandLine("git", "clone", "--branch", "main", "--single-branch", "--depth", "1",
                      repoUrl, "infra/repos/$repoName")
        }
    }
}

// Application 태스크
tasks.register<Exec>("appUp") {
    group = "docker"
    description = "Start application services"
    dependsOn("infraUp")
    workingDir = file("infra")

    doFirst {
        cloneRepoIfNotExists("atdd-camping-kiosk", "git@github.com:next-step/atdd-camping-kiosk.git")
        cloneRepoIfNotExists("atdd-camping-admin", "git@github.com:suzhanlee/atdd-camping-admin.git")
        cloneRepoIfNotExists("atdd-camping-reservation", "git@github.com:suzhanlee/atdd-camping-reservation.git")
    }

    commandLine("docker", "compose", "up", "-d")
}

tasks.register<Exec>("appDown") {
    group = "docker"
    description = "Stop application services"
    workingDir = file("infra")
    commandLine("docker", "compose", "down")
    finalizedBy("infraDown")
}