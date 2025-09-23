import java.io.ByteArrayOutputStream

plugins {
    java
}

group = "com.camping"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

// Versions
val cucumberVersion = "7.14.0"
val restAssuredVersion = "5.3.2"
val jacksonVersion = "2.17.2"

dependencies {
    // Cucumber
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    // RestAssured
    testImplementation("io.rest-assured:rest-assured:${restAssuredVersion}")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")

    // JUnit Jupiter
    testImplementation("org.junit.platform:junit-platform-suite:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.test {
    useJUnitPlatform()
}

data class GitRepo(val url: String, val dir: String, val branch: String = "main")

tasks.register("setupRepos") {
    group = "repo"
    description = "Create repos dir, clone missing repos, and pull updates if already cloned."

    doLast {
        val reposDir = file("${rootProject.projectDir}/repos").also {
            if (!it.exists()) {
                it.mkdirs()
                println("Created repos directory: $it")
            }
        }

        val reposToClone = listOf(
            GitRepo("https://github.com/next-step/atdd-camping-kiosk.git",       "atdd-camping-kiosk",       "main"),
            GitRepo("https://github.com/next-step/atdd-camping-admin.git",       "atdd-camping-admin",       "main"),
            GitRepo("https://github.com/next-step/atdd-camping-reservation.git", "atdd-camping-reservation", "main"),
        )

        fun execCapture(vararg cmd: String, wd: File? = null): String {
            val out = ByteArrayOutputStream()
            project.exec {
                if (wd != null) workingDir = wd
                commandLine(*cmd)
                standardOutput = out
                isIgnoreExitValue = true
            }
            return out.toString().trim()
        }
        fun execOrFail(vararg cmd: String, wd: File? = null) {
            project.exec {
                if (wd != null) workingDir = wd
                commandLine(*cmd)
            }
        }

        reposToClone.forEach { repo ->
            val repoDir = reposDir.resolve(repo.dir)

            if (!repoDir.exists()) {
                println("Cloning ${repo.url} into $repoDir (branch: ${repo.branch})")
                execOrFail("git", "clone", "--branch", repo.branch, repo.url, repoDir.absolutePath)
            } else {
                val isGit = repoDir.resolve(".git").exists()
                if (!isGit) {
                    println("⚠️  $repoDir exists but is not a git repository. Skipping.")
                    return@forEach
                }

                val origin = execCapture("git", "-C", repoDir.absolutePath, "remote", "get-url", "origin")
                if (origin.isNotBlank() && origin != repo.url) {
                    println("⚠️  origin url ($origin) != expected (${repo.url}) for $repoDir")
                }

                println("Updating repo: $repoDir (branch: ${repo.branch})")
                execOrFail("git", "-C", repoDir.absolutePath, "fetch", "--all", "--prune")
                execOrFail("git", "-C", repoDir.absolutePath, "checkout", repo.branch)
                project.exec {
                    commandLine("git", "-C", repoDir.absolutePath, "pull", "--rebase", "--autostash", "origin", repo.branch)
                    isIgnoreExitValue = true
                }
            }
        }
    }
}

tasks.register<Exec>("composeUp") {
    group = "docker"
    description = "docker compose up -d --build (atdd-tests)"
    commandLine(
            "docker", "compose",
            "-f", "infra/docker-compose.yml",
            "up", "-d", "--build"
    )
}


tasks.register<Exec>("composePs") {
    group = "docker"
    description = "docker compose ps (atdd-tests)"
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "ps")
}

tasks.register<Exec>("composeLogs") {
    group = "docker"
    description = "docker logs kiosk-app --tail 100"
    commandLine("docker", "logs", "kiosk-app", "--tail", "100")
}

tasks.register<Exec>("composeDown") {
    group = "docker"
    description = "docker compose down -v (atdd-tests)"
    commandLine("docker", "compose", "-f", "infra/docker-compose.yml", "down", "-v")
}


