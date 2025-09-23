val reposDir = File("${project.rootDir}/repos")

data class ServiceConfig(
    val name: String,
    val repoUrl: String,
    val branchName: String = "parkSeryu"
)

val services = listOf(
    ServiceConfig("kiosk", "https://github.com/ParkSeryu/atdd-camping-kiosk.git"),
    ServiceConfig("admin", "https://github.com/ParkSeryu/atdd-camping-admin.git"),
    ServiceConfig("reservation", "https://github.com/ParkSeryu/atdd-camping-reservation.git")
)

fun setupRepository(
    project: Project,
    config: ServiceConfig
) {
    val targetDir = File("${reposDir}/atdd-camping-${config.name}")

    if (!targetDir.parentFile.exists()) {
        println("Creating repos directory")
        targetDir.parentFile.mkdir()
    }

    if (!targetDir.exists()) {
        cloneRepository(project, config.repoUrl, targetDir)
    } else {
        updateRepository(project, targetDir, config.branchName)
    }

    checkoutBranch(project, targetDir, config.branchName)
    println("${config.name} repository setup completed")
}

fun cloneRepository(project: Project, repoUrl: String, targetDir: File) {
    println("Cloning repository...")
    project.exec {
        commandLine("git", "clone", repoUrl, targetDir.absolutePath)
    }
    project.exec {
        commandLine("git", "-C", targetDir.absolutePath, "fetch", "origin")
    }
}

fun updateRepository(project: Project, targetDir: File, branchName: String) {
    println("Repository exists. Pulling latest changes...")
    project.exec {
        commandLine("git", "-C", targetDir.absolutePath, "fetch", "origin")
    }
    project.exec {
        commandLine("git", "-C", targetDir.absolutePath, "pull", "origin", branchName)
        isIgnoreExitValue = true
    }
}

fun checkoutBranch(project: Project, targetDir: File, branchName: String) {
    val remoteBranchExists = project.exec {
        commandLine("git", "-C", targetDir.absolutePath, "show-ref", "--verify", "--quiet", "refs/remotes/origin/$branchName")
        isIgnoreExitValue = true
    }

    if (remoteBranchExists.exitValue == 0) {
        println("Checking out $branchName branch from origin...")
        project.exec {
            commandLine("git", "-C", targetDir.absolutePath, "checkout", "-B", branchName, "origin/$branchName")
        }
    } else {
        println("Creating and checking out $branchName branch...")
        project.exec {
            commandLine("git", "-C", targetDir.absolutePath, "checkout", "-b", branchName)
        }
    }
}

tasks.register("SetupAllServices") {
    group = "setup"
    description = "Sets up all services (kiosk, admin, reservation)."

    doLast {
        services.forEach { service ->
            setupRepository(project, service)
        }
        println("All services setup completed")
    }
}
