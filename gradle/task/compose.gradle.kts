data class ComposeConfig(
    val name: String,
    val filePath: String,
    val description: String
)

val composeConfigs = listOf(
    ComposeConfig("Infra", "infra/docker-compose-infra.yml", "Infrastructure services"),
    ComposeConfig("Was", "infra/docker-compose.yml", "Application services")
)

/**
 * Docker Compose 서비스를 실행하는 공통 함수
 */
fun Project.executeDockerCompose(
    config: ComposeConfig,
    vararg commands: String
): Boolean {
    val composeFile = file(config.filePath)

    if (!composeFile.exists()) {
        return false
    }

    return try {
        val result = exec {
            commandLine("docker", "compose", "-f", config.filePath, *commands)
            isIgnoreExitValue = true
        }
        result.exitValue == 0
    } catch (e: Exception) {
        false
    }
}

tasks.register("ComposeAllUp") {
    group = "compose"
    description = "Start all docker compose services in correct order"

    doLast {
        composeConfigs.forEach { config ->
            project.executeDockerCompose(config, "up", "-d", "--build")
        }
    }
}

tasks.register("ComposeAllDown") {
    group = "compose"
    description = "Stop all docker compose services in reverse order"

    doLast {
        composeConfigs.reversed().forEach { config ->
            project.executeDockerCompose(config, "down", "-v")
        }
    }
}
