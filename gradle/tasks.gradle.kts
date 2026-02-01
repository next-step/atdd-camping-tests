// =================================================================
// Docker Compose Tasks
// =================================================================

// --- Infra Tasks (docker-compose-infra.yml) ---
tasks.register<Exec>("InfraComposeUp") {
    group = "docker"
    description = "Run infra services via docker compose"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "up", "-d"
    )
}

tasks.register<Exec>("InfraComposeDown") {
    group = "docker"
    description = "Stop infra services and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose-infra.yml",
        "down", "-v"
    )
    dependsOn("ServiceComposeDown") // 서비스가 먼저 내려가야 함
}

// --- Service Tasks (docker-compose.yml) ---
tasks.register<Exec>("ServiceComposeUp") {
    group = "docker"
    description = "Run application services via docker compose (build + up)"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "up", "-d", "--build"
    )
    dependsOn("InfraComposeUp") // 인프라가 먼저 실행되어야 함
}

tasks.register<Exec>("ServiceComposeDown") {
    group = "docker"
    description = "Stop application services and remove volumes"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "down", "-v"
    )
}

// --- Helper Tasks ---
tasks.register<Exec>("ServicePs") {
    group = "docker"
    description = "Show application service container status"
    commandLine(
        "docker", "compose",
        "-f", "infra/docker-compose.yml",
        "ps"
    )
}

tasks.register<Exec>("ServiceLog") {
    group = "docker"
    description = "Show logs for a specific service container"
    commandLine(
        "docker", "logs", "atdd-kiosk",
        "--tail", "100", "-f"
    )
}

// --- Master Tasks ---
tasks.register("composeUp") {
    group = "docker"
    description = "Bring up all infra and application services in the correct order."
    dependsOn("ServiceComposeUp")
}

tasks.register("composeDown") {
    group = "docker"
    description = "Bring down all application and infra services in the correct order."
    dependsOn("InfraComposeDown")
}
