tasks.register<Exec>("WasComposeUp") {
         group = "compose"
         description = "Run WAS docker compose (build + up)"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "up", "-d", "--build"
         )
     }

 tasks.register<Exec>("WasComposeDown") {
         group = "compose"
         description = "Stop WAS compose and remove volumes"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "down", "-v"
         )
     }

tasks.register<Exec>("InfraComposeUp") {
    group = "compose"
    description = "Run Infra via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "up", "-d", "--build"
    )
}

tasks.register<Exec>("InfraComposeDown") {
    group = "compose"
    description = "Stop Infra compose and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "down", "-v"
    )
}

