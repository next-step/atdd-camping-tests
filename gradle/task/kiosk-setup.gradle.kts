val reposDir = File("${project.rootDir}/repos")
val serviceDir = File("${reposDir}/atdd-camping-kiosk")

tasks.register<Exec>("CloneKioskRepo") {
    group = "setup kiosk"
    description = "Clones the atdd-camping-kiosk repository if it doesn't exist."

    doFirst {
        if (!reposDir.exists()) {
            println("Creating repos directory")
            reposDir.mkdir()
        }
    }

    onlyIf {
        !serviceDir.exists()
    }

    commandLine("git", "clone", "https://github.com/next-step/atdd-camping-kiosk.git", serviceDir.absolutePath)
}

tasks.register<Exec>("PullKioskRepo") {
    group = "setup kiosk"
    description = "Pulls the latest changes for atdd-camping-kiosk repository if it exists."

    onlyIf {
        serviceDir.exists()
    }

    commandLine("git", "-C", serviceDir.absolutePath, "pull")
}

tasks.register<Exec>("CheckoutKioskMainBranch") {
    group = "setup kiosk"
    description = "Checks out the main branch of the atdd-camping-kiosk repository."

    dependsOn("CloneKioskRepo", "PullKioskRepo")

    commandLine("git", "-C", serviceDir.absolutePath, "checkout", "main")
}

tasks.register("SetupKiosk") {
    group = "setup kiosk"
    description = "Sets up the atdd-camping-kiosk by cloning/pulling and checking out the main branch."
    dependsOn("CheckoutKioskMainBranch")
    doLast {
        println("Service code setup completed")
    }
}
