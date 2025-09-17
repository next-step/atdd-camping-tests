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

tasks.register<Exec>("SetupKiosk"){
    group = "setup kiosk"
    description = "Setup Kiosk by cloneing the repository and running the setup script"

    val reposDir = File("${project.rootDir}/repos")
    val serviceDir = File("${reposDir}/atdd-camping-kiosk")

    doFirst {
        if (!reposDir.exists()) {
            println("Creating repos directory")
            reposDir.mkdir()
        }
    }

    commandLine = if (serviceDir.exists()) {
        println("Repository already exists, pulling latest changes")
        listOf("sh", "-c", "cd $serviceDir && git pull && git checkout main")
    } else {
        println("Cloning repository")
        listOf("sh", "-c", "git clone https://github.com/next-step/atdd-camping-kiosk.git ${serviceDir.absolutePath} && cd $serviceDir && git checkout main")
    }

    doLast {
        println("Service code setup completed")
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

