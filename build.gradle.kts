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
    testImplementation("io.cucumber:cucumber-picocontainer:$cucumberVersion")

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
    testImplementation("mysql:mysql-connector-java:8.0.33")
}

tasks.test {
    useJUnitPlatform()
}

// === Infra Tasks ===
tasks.register<Exec>("composeUp") {
    group = "infra"
    description = "Start kiosk service via docker compose"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "up", "-d", "--build")
}

tasks.register<Exec>("composeDown") {
    group = "infra"
    description = "Stop kiosk service and remove volumes"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "down", "-v")
}

tasks.register<Exec>("composePs") {
    group = "infra"
    description = "Show running containers status"
    commandLine("docker-compose", "-f", "infra/docker-compose.yml", "ps")
}

// 서비스 준비 대기
tasks.register("waitForKiosk") {
    group = "infra"
    description = "Wait for kiosk service to be ready"
    doLast {
        val maxRetries = 30
        val retryInterval = 2000L
        val kioskUrl = "http://localhost:18081/"

        println("Waiting for Kiosk service at $kioskUrl...")

        repeat(maxRetries) { i ->
            try {
                val exitCode = exec {
                    commandLine("curl", "-f", "-s", "-o", "/dev/null", kioskUrl)
                    isIgnoreExitValue = true
                }.exitValue

                if (exitCode == 0) {
                    println("✓ Kiosk service is ready! (attempt: ${i + 1})")
                    return@doLast
                }
            } catch (e: Exception) {
                // continue
            }

            println("  Waiting... (${i + 1}/$maxRetries)")
            Thread.sleep(retryInterval)
        }

        throw GradleException("Kiosk service did not become ready after $maxRetries attempts")
    }
}

tasks.register<Test>("smokeTest") {
    group = "verification"
    description = "Run smoke tests only"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@smoke")
}



// 인프라 (DB) 기동
tasks.register<Exec>("infraUp") {
    group = "infra"
    description = "Start infrastructure (MySQL DB) via docker compose"
    commandLine("docker-compose", "-f", "infra/docker-compose-infra.yml", "up", "-d")
}

// 인프라 종료
tasks.register<Exec>("infraDown") {
    group = "infra"
    description = "Stop infrastructure and remove volumes"
    commandLine("docker-compose", "-f", "infra/docker-compose-infra.yml", "down", "-v")
}

// 인프라 상태 확인
tasks.register<Exec>("infraPs") {
    group = "infra"
    description = "Show infrastructure status"
    commandLine("docker-compose", "-f", "infra/docker-compose-infra.yml", "ps")
}

// 인프라 준비 대기
tasks.register("waitForInfra") {
    group = "infra"
    description = "Wait for infrastructure (DB) to be ready"
    doLast {
        val maxRetries = 20
        val retryInterval = 3000L

        println("Waiting for MySQL DB...")

        repeat(maxRetries) { i ->
            try {
                val exitCode = exec {
                    commandLine("docker", "exec", "atdd-db", "mysqladmin", "ping", "-h", "127.0.0.1", "-uroot", "-psecret")
                    isIgnoreExitValue = true
                }.exitValue

                if (exitCode == 0) {
                    println("✓ MySQL DB is ready! (attempt: ${i + 1})")
                    return@doLast
                }
            } catch (e: Exception) {
                // continue
            }

            println("  Waiting... (${i + 1}/$maxRetries)")
            Thread.sleep(retryInterval)
        }

        throw GradleException("MySQL DB did not become ready")
    }
}

// 다중 서비스 준비 대기
tasks.register("waitForServices") {
    group = "infra"
    description = "Wait for all services to be ready"
    doLast {
        val services = mapOf(
            "Kiosk" to "http://localhost:18081/",
            "Admin" to "http://localhost:18082/",
            "Reservation" to "http://localhost:18083/"
        )

        services.forEach { (name, url) ->
            println("Waiting for $name at $url...")

            val maxRetries = 30
            val retryInterval = 2000L

            repeat(maxRetries) { i ->
                try {
                    val exitCode = exec {
                        commandLine("curl", "-f", "-s", "-o", "/dev/null", url)
                        isIgnoreExitValue = true
                    }.exitValue

                    if (exitCode == 0) {
                        println("✓ $name is ready! (attempt: ${i + 1})")
                        return@forEach
                    }
                } catch (e: Exception) {
                    // continue
                }

                println("  Waiting for $name... (${i + 1}/$maxRetries)")
                Thread.sleep(retryInterval)
            }

            throw GradleException("$name did not become ready")
        }
    }
}

// 다중 서비스 Smoke 테스트
tasks.register<Test>("multiSmokeTest") {
    group = "verification"
    description = "Run multi-service smoke tests"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@multi-service")
}

// E2E 테스트
tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "Run E2E tests only"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@e2e")
}

// 전체 테스트 (smoke + e2e)
tasks.register<Test>("allTests") {
    group = "verification"
    description = "Run all tests (smoke + e2e)"
    useJUnitPlatform()
}