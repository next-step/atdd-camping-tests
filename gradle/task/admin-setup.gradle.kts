val reposDir = File("${project.rootDir}/repos")
val serviceDir = File("${reposDir}/atdd-camping-admin")

tasks.register<Exec>("CloneAdminRepo") {
    group = "setup admin"
    description = "Clones the atdd-camping-admin repository if it doesn't exist."

    doFirst {
        if (!reposDir.exists()) {
            println("Creating repos directory")
            reposDir.mkdir()
        }
    }

    onlyIf {
        !serviceDir.exists()
    }

    commandLine("git", "clone", "https://github.com/next-step/atdd-camping-admin.git", serviceDir.absolutePath)
}

tasks.register<Exec>("PullAdminRepo") {
    group = "setup admin"
    description = "Pulls the latest changes for atdd-camping-admin repository if it exists."

    onlyIf {
        serviceDir.exists()
    }

    commandLine("git", "-C", serviceDir.absolutePath, "pull")
}

tasks.register<Exec>("CheckoutAdminMainBranch") {
    group = "setup admin"
    description = "Checks out the main branch of the atdd-camping-admin repository."

    dependsOn("CloneAdminRepo", "PullAdminRepo")

    commandLine("git", "-C", serviceDir.absolutePath, "checkout", "main")
}

tasks.register("SetupAdmin") {
    group = "setup admin"
    description = "Sets up the atdd-camping-admin by cloning/pulling and checking out the main branch."
    dependsOn("CheckoutAdminMainBranch")
    doLast {
        println("Service code setup completed")
    }
}
