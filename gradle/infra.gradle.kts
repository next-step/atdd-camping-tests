// Infra (DB 등)
tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (DB, etc.)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml up -d")
}

tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose-infra.yml down -v")
}

// Services (kiosk, admin, reservation)
tasks.register<Exec>("servicesUp") {
    group = "infra"
    description = "Start services via docker compose (build + up)"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml up -d --build")
}

tasks.register<Exec>("servicesDown") {
    group = "infra"
    description = "Stop services and remove volumes"
    commandLine("sh", "-c", "docker compose -f infra/docker-compose.yml down -v")
}

// All (infra + services)
tasks.register("allUp") {
    group = "infra"
    description = "Start all (infra + services)"
    dependsOn("infraUp", "servicesUp")
}

tasks.register("allDown") {
    group = "infra"
    description = "Stop all (infra + services)"
    dependsOn("infraDown", "servicesDown")
}