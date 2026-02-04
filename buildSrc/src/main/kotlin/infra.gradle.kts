data class RepoConfig(val dir: String, val repo: String, val branch: String)

val repos = listOf(
    RepoConfig("atdd-camping-reservation", "https://github.com/yhh1056/atdd-camping-reservation.git", "yhh1056"),
    RepoConfig("atdd-camping-admin", "https://github.com/yhh1056/atdd-camping-admin.git", "yhh1056"),
    RepoConfig("atdd-camping-kiosk", "https://github.com/yhh1056/atdd-camping-kiosk.git", "yhh1056")
)

fun runCommand(vararg args: String, workingDir: File): Int {
    return ProcessBuilder(*args)
        .directory(workingDir)
        .inheritIO()
        .start()
        .waitFor()
}

tasks.register("setup") {
    group = "infra"
    description = "Clone or update all project repositories"

    doFirst {
        val reposDir = file("repos")
        reposDir.mkdirs()

        repos.forEach { config ->
            val repoDir = file("repos/${config.dir}")
            if (repoDir.resolve(".git").exists()) {
                println("a🔄 ${config.dir} 이미 존재 → pull")
                runCommand("git", "fetch", "origin", workingDir = repoDir)
                runCommand("git", "checkout", config.branch, workingDir = repoDir)
                runCommand("git", "pull", "origin", config.branch, workingDir = repoDir)
            } else {
                println("📦 ${config.dir} clone")
                runCommand("git", "clone", config.repo, "--branch", config.branch, "--single-branch", workingDir = reposDir)
            }
        }
        println("✅ 모든 프로젝트 최신 상태로 준비 완료")
    }
}

tasks.register<Exec>("dbComposeUp") {
    group = "infra"
    description = "Start database containers"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "up", "-d", "--build", "--wait")
}

tasks.register<Exec>("dbComposeDown") {
    group = "infra"
    description = "Stop database containers"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "down", "-v")
}

tasks.register<Exec>("applicationComposeUp") {
    group = "infra"
    description = "Start application containers"

    doFirst {
        println("🧹 cleaning docker compose")
    }

    dependsOn("setup", "dbComposeUp")
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "up", "-d", "--build", "--wait")
}

tasks.register<Exec>("applicationComposeDown") {
    group = "infra"
    description = "Stop application containers"
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "down", "-v")
    finalizedBy("dbComposeDown")
}

tasks.register<Exec>("composeCleanup") {
    group = "infra"
    description = "Always cleanup docker compose"

    isIgnoreExitValue = true

    commandLine(
        "sh", "-c",
        """
        docker compose -f infra/docker-compose.yml down -v || true
        docker compose -f infra/docker-compose-infra.yml down -v || true
        """.trimIndent()
    )
}
