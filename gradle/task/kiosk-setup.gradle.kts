val reposDir = File("${project.rootDir}/repos")
val serviceDir = File("${reposDir}/atdd-camping-kiosk")

fun setupRepository(
    project: Project,
    repoUrl: String,
    targetDir: File,
    branchName: String = "parkSeryu"
) {
    if (!targetDir.parentFile.exists()) {
        println("Creating repos directory")
        targetDir.parentFile.mkdir()
    }

    if (!targetDir.exists()) {
        cloneRepository(project, repoUrl, targetDir)
    } else {
        updateRepository(project, targetDir, branchName)
    }

    checkoutBranch(project, targetDir, branchName)
    println("Repository setup completed")
}

fun cloneRepository(project: Project, repoUrl: String, targetDir: File) {
    println("Cloning repository...")
    project.exec {
        commandLine("git", "clone", repoUrl, targetDir.absolutePath)
    }
    // 클론 직후 원격 브랜치 정보 업데이트
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

tasks.register("SetupKioskRepo") {
    group = "setup kiosk"
    description = "Clones or pulls the atdd-camping-kiosk repository and checks out parkSeryu branch."

    doLast {
        setupRepository(
            project,
            "https://github.com/ParkSeryu/atdd-camping-kiosk.git",
            serviceDir
        )
    }
}

tasks.register("SetupKiosk") {
    group = "setup kiosk"
    description = "Sets up the atdd-camping-kiosk by cloning/pulling and checking out the parkSeryu branch."
    dependsOn("SetupKioskRepo")
    doLast {
        println("Service code setup completed")
    }
}
