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

tasks.register("setupRepos") {
    doLast {
        val reposDir = file("${rootProject.projectDir}/repos")
        if (!reposDir.exists()) {
            reposDir.mkdirs()
            println("Created repos directory: $reposDir")
        }

        val gitignore = file("${rootProject.projectDir}/.gitignore")
        val ignoreEntry = "repos/"

        if (!gitignore.exists()) {
            gitignore.createNewFile()
            println(".gitignore created")
        }

        val lines = gitignore.readLines()
        if (!lines.contains(ignoreEntry)) {
            gitignore.appendText(ignoreEntry + "\n")
            println("Added '$ignoreEntry' to .gitignore")
        } else {
            println("'$ignoreEntry' already exists in .gitignore")
        }

        val reposToClone = listOf(
                "https://github.com/next-step/atdd-camping-kiosk.git" to "atdd-camping-kiosk",
        )

        reposToClone.forEach { (repoUrl, dirName) ->
            val repoDir = reposDir.resolve(dirName)
            if (!repoDir.exists()) {
                println("Cloning $repoUrl into $repoDir")
                exec {
                    commandLine("git", "clone", repoUrl, repoDir.absolutePath)
                }
            } else {
                println("Repo already exists: $repoDir")
            }
        }
    }
}

