tasks.register<Exec>("kioskComposeUp") {
         group = "infra"
         description = "Run kiosk via docker compose (build + up)"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "up", "-d", "--build"
         )
     }

 tasks.register<Exec>("kioskComposeDown") {
         group = "infra"
         description = "Stop kiosk compose and remove volumes"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "down", "-v"
         )
     }
