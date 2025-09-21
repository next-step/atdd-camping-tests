import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Delete

// Default compose file path used by docker-related tasks
val appComposeFile = project.rootProject.layout.projectDirectory
    .file("infra/docker-compose.yml").asFile.absolutePath

// Infra compose file path (for DB, etc.)
val infraComposeFile = project.rootProject.layout.projectDirectory
    .file("infra/docker-compose-infra.yml").asFile.absolutePath

// compose 파일 경로들 정의
val allComposeFiles = listOf(
    project.rootProject.layout.projectDirectory.file("infra/docker-compose.yml").asFile.absolutePath,
    project.rootProject.layout.projectDirectory.file("infra/docker-compose-infra.yml").asFile.absolutePath
)

// DB up (infra only)
tasks.register<Exec>("db-up") {
    group = "docker"
    description = "Run 'docker compose -f infra/docker-compose-infra.yml up -d db' to start only the DB container"
    commandLine(
        "docker", "compose",
        "-f", infraComposeFile,
        "up", "-d", "db"
    )
}

// DB만 중지 (컨테이너는 남김)
tasks.register<Exec>("db-stop") {
    group = "docker"
    description = "docker compose -f infra/docker-compose-infra.yml stop db"
    commandLine(
        "docker", "compose",
        "-f", infraComposeFile,
        "stop", "db"
    )
}

// DB 컨테이너 제거(이미 중지되어 있어야 함; -f로 강제 제거)
tasks.register<Exec>("db-rm") {
    group = "docker"
    description = "docker compose -f infra/docker-compose-infra.yml rm -f db"
    commandLine(
        "docker", "compose",
        "-f", infraComposeFile,
        "rm", "-f", "db"
    )
}

// 편의 태스크: DB만 내리기(중지+제거)
tasks.register("db-down") {
    group = "docker"
    description = "Stop and remove only the DB service containers"
    dependsOn("db-stop", "db-rm")
}

// 인프라 전체 down (네트워크 등도 함께 정리)
tasks.register<Exec>("infra-down-all") {
    group = "docker"
    description = "docker compose -f infra/docker-compose-infra.yml down (stack-wide)"
    commandLine(
        "docker", "compose",
        "-f", infraComposeFile,
        "down"
    )
}

// Application up (including image build)
tasks.register<Exec>("app-up") {
    group = "docker"
    description = "Run 'docker compose up -d --build' for the specified compose file (default: infra/docker-compose.yml)"
    dependsOn("db-up")
    commandLine(
        "docker", "compose",
        "-f", appComposeFile,
        "up", "-d", "--build"
    )
}

// Application down
tasks.register<Exec>("app-down") {
    group = "docker"
    description = "Run 'docker compose down' for the specified compose file (default: infra/docker-compose.yml)"
    commandLine(
        "docker", "compose",
        "-f", appComposeFile,
        "down"
    )
}

// Status check (ps)
tasks.register<Exec>("ps") {
    group = "docker"
    description = "docker-compose.yml 두 개를 병합하여 ps 실행"
    commandLine(
        "docker", "compose",
        "-p", "atdd",
        "-f", infraComposeFile,
        "-f", appComposeFile,
        "ps"
    )
}

// Kiosk Logs
tasks.register<Exec>("kiosk-logs") {
    group = "docker"
    description = "Show last 100 lines of kiosk container logs. Default container: 'atdd-kiosk'"
    commandLine(
        "docker", "logs",
        "atdd-kiosk", "--tail", "100"
    )
}

// Admin Logs
tasks.register<Exec>("admin-logs") {
    group = "docker"
    description = "Show last 100 lines of admin container logs. Default container: 'atdd-admin'"
    commandLine(
        "docker", "logs",
        "atdd-admin", "--tail", "100"
    )
}

// Reservation Logs
tasks.register<Exec>("reservation-logs") {
    group = "docker"
    description = "Show last 100 lines of reservation container logs. Default container: 'atdd-reservation'"
    commandLine(
        "docker", "logs",
        "atdd-reservation", "--tail", "100"
    )
}

tasks.register("repo-clone") {
    group = "docker"
    description = "저장소 완전 초기화 (기존 삭제 후 새로 클론)"

    doLast {
        println("🔄 저장소 완전 초기화를 시작합니다...")

        DockerConfig.repos.forEach { repo ->
            println("📦 ${repo.name} 완전 초기화 중...")

            val sourceDir = file("repo/${repo.name}")
            if (sourceDir.exists()) {
                sourceDir.deleteRecursively()
            }
            sourceDir.mkdirs()

            exec {
                workingDir = sourceDir
                commandLine = listOf("git", "clone", "-b", repo.branch, repo.url, sourceDir.absolutePath)
            }
            println("✅ ${repo.name} 클론 완료")
        }

        println("🎉 모든 저장소 완전 초기화가 완료되었습니다!")
    }
}

data class Repo(
    val name: String,
    val url: String,
    val branch: String
)

object DockerConfig {
    val repos = listOf(
        Repo(
            name = "atdd-camping-kiosk",
            url = "https://github.com/mdy0501/atdd-camping-kiosk.git",
            branch = "mdy0501-test"
        ),
        Repo(
            name = "atdd-camping-admin",
            url = "https://github.com/mdy0501/atdd-camping-admin.git",
            branch = "mdy0501-test"
        ),
        Repo(
            name = "atdd-camping-reservation",
            url = "https://github.com/mdy0501/atdd-camping-reservation.git",
            branch = "mdy0501-test"
        ),

    )
}