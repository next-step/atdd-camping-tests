// Docker Compose 태스크
tasks.register<Exec>("composeUp") {
    group = "docker"
    description = "Start all services"
    workingDir = file("infra")
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
}