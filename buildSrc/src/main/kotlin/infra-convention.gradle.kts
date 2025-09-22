// Infrastructure 태스크
tasks.register<Exec>("infraUp") {
    group = "docker"
    description = "Start infrastructure services (DB)"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "up", "-d")
}

tasks.register<Exec>("infraDown") {
    group = "docker"
    description = "Stop infrastructure services (DB)"
    workingDir = file("infra")
    commandLine("docker", "compose", "-f", "docker-compose-infra.yml", "down")
}