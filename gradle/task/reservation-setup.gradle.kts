val reposDir = File("${project.rootDir}/repos")
val serviceDir = File("${reposDir}/atdd-camping-reservation")

tasks.register<Exec>("CloneReservationRepo") {
    group = "setup reservation"
    description = "Clones the atdd-camping-reservation repository if it doesn't exist."

    doFirst {
        if (!reposDir.exists()) {
            println("Creating repos directory")
            reposDir.mkdir()
        }
    }

    onlyIf {
        !serviceDir.exists()
    }

    commandLine("git", "clone", "https://github.com/next-step/atdd-camping-reservation.git", serviceDir.absolutePath)
}

tasks.register<Exec>("PullReservationRepo") {
    group = "setup reservation"
    description = "Pulls the latest changes for atdd-camping-reservation repository if it exists."

    onlyIf {
        serviceDir.exists()
    }

    commandLine("git", "-C", serviceDir.absolutePath, "pull")
}

tasks.register<Exec>("CheckoutReservationMainBranch") {
    group = "setup reservation"
    description = "Checks out the main branch of the atdd-camping-reservation repository."

    dependsOn("CloneReservationRepo", "PullReservationRepo")

    commandLine("git", "-C", serviceDir.absolutePath, "checkout", "main")
}

tasks.register("SetupReservation") {
    group = "setup reservation"
    description = "Sets up the atdd-camping-reservation by cloning/pulling and checking out the main branch."
    dependsOn("CheckoutReservationMainBranch")
    doLast {
        println("Service code setup completed")
    }
}
