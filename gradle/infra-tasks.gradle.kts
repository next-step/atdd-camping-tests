// Infrastructure Tasks for ATDD Test Environment

val kioskRepoDir = file("repos/atdd-camping-kiosk")
val adminRepoDir = file("repos/atdd-camping-admin")
val reservationRepoDir = file("repos/atdd-camping-reservation")
val composeProject = "atdd-infra"
val composeFile = "infra/docker-compose.yml"

val targetBranch = project.findProperty("branch")?.toString() ?: "main"

fun dockerCompose(vararg args: String) = listOf("docker", "compose", "-p", composeProject, "-f", composeFile) + args

fun createCloneTasks(
    prefix: String,
    repoDir: File,
    repoUrl: String,
) {
    tasks.register<Exec>("${prefix}CloneNew") {
        group = "infra"
        description = "Clone $prefix repository (if not exists)"
        onlyIf { !repoDir.resolve(".git").exists() }
        commandLine("git", "clone", "--branch", targetBranch, "--single-branch", "--depth", "1", repoUrl, repoDir.absolutePath)
        doLast { println("[OK] Cloned $prefix on branch: $targetBranch") }
    }

    tasks.register<Exec>("${prefix}Fetch") {
        group = "infra"
        description = "Fetch $prefix repository updates"
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "fetch", "origin", targetBranch)
    }

    tasks.register<Exec>("${prefix}Switch") {
        group = "infra"
        description = "Switch to target branch"
        dependsOn("${prefix}Fetch")
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "switch", targetBranch)
        doLast { println("[OK] Switched $prefix to branch: $targetBranch") }
    }

    tasks.register<Exec>("${prefix}Pull") {
        group = "infra"
        description = "Pull latest changes"
        dependsOn("${prefix}Switch")
        onlyIf { repoDir.resolve(".git").exists() }
        workingDir = repoDir
        commandLine("git", "pull", "--rebase")
    }

    tasks.register("${prefix}Clone") {
        group = "infra"
        description = "Clone or update $prefix repository"
        dependsOn("${prefix}CloneNew", "${prefix}Pull")
        doLast { println("[OK] $prefix repo synced on branch: $targetBranch") }
    }
}

createCloneTasks("kiosk", kioskRepoDir, "https://github.com/next-step/atdd-camping-kiosk")
createCloneTasks("admin", adminRepoDir, "https://github.com/next-step/atdd-camping-admin")
createCloneTasks("reservation", reservationRepoDir, "https://github.com/next-step/atdd-camping-reservation")

tasks.register<Exec>("kioskCheckDocker") {
    group = "infra"
    description = "Check Docker daemon is running"
    commandLine("docker", "info")
}

tasks.register("cloneAll") {
    group = "infra"
    description = "Clone or update all repositories (admin, reservation, kiosk)"
    dependsOn("adminClone", "reservationClone", "kioskClone")
}

tasks.register<Exec>("kioskBuild") {
    group = "infra"
    description = "Build kiosk JAR"
    dependsOn("kioskClone")
    doFirst { kioskRepoDir.resolve("gradlew").takeIf { it.exists() }?.setExecutable(true) }
    workingDir = kioskRepoDir
    commandLine("./gradlew", "clean", "build", "-x", "test", "--warning-mode", "all")
}

tasks.register<Exec>("adminBuild") {
    group = "infra"
    description = "Build admin JAR"
    dependsOn("adminClone")
    doFirst { adminRepoDir.resolve("gradlew").takeIf { it.exists() }?.setExecutable(true) }
    workingDir = adminRepoDir
    commandLine("./gradlew", "clean", "build", "-x", "test", "--warning-mode", "all")
}

tasks.register<Exec>("reservationBuild") {
    group = "infra"
    description = "Build reservation JAR"
    dependsOn("reservationClone")
    doFirst { reservationRepoDir.resolve("gradlew").takeIf { it.exists() }?.setExecutable(true) }
    workingDir = reservationRepoDir
    commandLine("./gradlew", "clean", "build", "-x", "test", "--warning-mode", "all")
}

tasks.register("buildAll") {
    group = "infra"
    description = "Build all service JARs"
    dependsOn("kioskBuild", "adminBuild", "reservationBuild")
}

tasks.register<Exec>("kioskComposeUp") {
    group = "infra"
    description = "Start all services via docker compose"
    dependsOn("buildAll")
    workingDir = projectDir
    commandLine(dockerCompose("up", "-d", "--build", "--remove-orphans"))
}

tasks.register<Exec>("kioskStatus") {
    group = "infra"
    description = "Show kiosk container status"
    commandLine(dockerCompose("ps"))
}

tasks.register<Exec>("kioskLogs") {
    group = "infra"
    description = "Show kiosk logs"
    commandLine(dockerCompose("logs", "kiosk", "--tail=100"))
    isIgnoreExitValue = true
}

tasks.register("kioskUp") {
    group = "infra"
    description = "Clone, build, and start kiosk service via docker compose"
    dependsOn("kioskCheckDocker", "kioskComposeUp", "kioskStatus", "kioskLogs")
    doLast { println("[OK] kiosk up.") }
}

tasks.register<Exec>("kioskDown") {
    group = "infra"
    description = "Stop kiosk compose and remove volumes"
    commandLine(dockerCompose("down", "-v"))
    doLast { println("[OK] kiosk down.") }
}
