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

tasks.register<Exec>("cloneKioskRepository") {
    description = "Clones the atdd-camping-kiosk repository"
    if (!file("repos").exists()) {
        println("create repos directory")
        file("repos").mkdir()
    }

    if (file("repos/atdd-camping-kiosk").exists()) {
        commandLine("git", "-C", "repos/atdd-camping-kiosk", "pull")
        return@register
    }
    commandLine("git", "clone", "--branch", "main", "--single-branch", "https://github.com/next-step/atdd-camping-kiosk", "repos/atdd-camping-kiosk")
}
