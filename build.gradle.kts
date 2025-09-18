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

tasks.register("PrepareKioskDirectory") {
    group = "setup kiosk"
    description = "Prepare directory for kiosk repository"

    val reposDir = File("${project.rootDir}/repos")

    doLast {
        if (!reposDir.exists()) {
            println("📁 Creating repos directory: ${reposDir.absolutePath}")
            reposDir.mkdir()
            println("✅ Directory created successfully")
        } else {
            println("📁 Repos directory already exists: ${reposDir.absolutePath}")
        }
    }
}

tasks.register<Exec>("CloneKioskRepository") {
    group = "setup kiosk"
    description = "Clone kiosk repository if it doesn't exist"

    dependsOn("PrepareKioskDirectory")

    val reposDir = File("${project.rootDir}/repos")
    val serviceDir = File("${reposDir}/atdd-camping-kiosk")

    onlyIf { !serviceDir.exists() }

    doFirst {
        println("🔄 Cloning repository from GitHub...")
    }

    commandLine = listOf("sh", "-c", "git clone https://github.com/next-step/atdd-camping-kiosk.git ${serviceDir.absolutePath} && cd $serviceDir && git checkout main")

    doLast {
        println("✅ Repository cloned successfully to: ${serviceDir.absolutePath}")
    }
}

tasks.register<Exec>("UpdateKioskRepository") {
    group = "setup kiosk"
    description = "Pull latest changes if repository already exists"

    dependsOn("PrepareKioskDirectory")

    val reposDir = File("${project.rootDir}/repos")
    val serviceDir = File("${reposDir}/atdd-camping-kiosk")

    onlyIf { serviceDir.exists() }

    doFirst {
        println("🔄 Updating existing repository...")
    }

    commandLine = listOf("sh", "-c", "cd $serviceDir && git pull && git checkout main")

    doLast {
        println("✅ Repository updated successfully")
    }
}

tasks.register("SetupKiosk") {
    group = "setup kiosk"
    description = "Setup Kiosk by cloning or updating the repository"

    dependsOn("PrepareKioskDirectory", "CloneKioskRepository", "UpdateKioskRepository")

    doLast {
        println("🎉 Kiosk service setup completed!")
        println("ℹ️ You can now run the kiosk service using: ./gradlew kioskComposeUp")
    }
}


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

