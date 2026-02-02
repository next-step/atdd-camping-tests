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

    dependsOn("dbComposeUp")
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
