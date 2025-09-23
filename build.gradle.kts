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

tasks.register("cloneRepository") {
    group = "setup"
    description = "Clone atdd-camping-kiosk repository"

    doLast {
        val reposDir = file("repos")
        val targetDir = file("repos/atdd-camping-kiosk")

        // repos 디렉토리 생성
        if (!reposDir.exists()) {
            reposDir.mkdirs()
            println("Created repos directory")
        }

        // 이미 클론된 경우 스킵
        if (targetDir.exists()) {
            println("Repository already exists at ${targetDir.absolutePath}")
            return@doLast
        }

        // Git 클론 실행
        val gitCloneCommand = listOf(
            "git", "clone",
            "https://github.com/next-step/atdd-camping-kiosk.git",
            "repos/atdd-camping-kiosk"
        )

        val process = ProcessBuilder(gitCloneCommand)
            .directory(projectDir)
            .inheritIO()
            .start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("Successfully cloned repository to repos/atdd-camping-kiosk")
        } else {
            throw GradleException("Failed to clone repository (exit code: $exitCode)")
        }
    }
}

// 테스트 컴파일 전에 자동으로 클론
tasks.named("compileTestJava") {
    dependsOn("cloneRepository")
}
