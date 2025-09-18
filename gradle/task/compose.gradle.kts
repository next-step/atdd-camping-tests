tasks.register<Exec>("dockerComposeUp") {
         group = "compose"
         description = "Run kiosk via docker compose (build + up)"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "up", "-d", "--build"
         )
     }

 tasks.register<Exec>("dockerComposeDown") {
         group = "compose"
         description = "Stop kiosk compose and remove volumes"
         commandLine(
                     "docker", "compose",
             "-f", "infra/docker-compose.yml",
             "down", "-v"
         )
     }
