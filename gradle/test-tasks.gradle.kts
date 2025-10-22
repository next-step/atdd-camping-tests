// Test Tasks for ATDD Test Execution

val testSourceSet = the<SourceSetContainer>()["test"]

tasks.register<Test>("testSmoke") {
    group = "verification"
    description = "Run smoke tests"
    useJUnitPlatform()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }

    environment(System.getenv())

    doFirst { println("[INFO] Running smoke tests...") }
    doLast { println("[OK] Smoke tests completed.") }
}

tasks.register<Test>("testAiCandidate") {
    group = "verification"
    description = "Run AI candidate tests (tests tagged with @ai-candidate)"
    useJUnitPlatform()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }

    systemProperty("cucumber.filter.tags", "@ai-candidate")
    environment(System.getenv())

    doFirst { println("[INFO] Running AI candidate tests...") }
    doLast { println("[OK] AI candidate tests completed.") }
}

tasks.register<Test>("testE2e") {
    group = "verification"
    description = "Run E2E tests (tests tagged with @e2e)"
    useJUnitPlatform()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }

    systemProperty("cucumber.filter.tags", "@e2e")
    environment(System.getenv())

    doFirst { println("[INFO] Running E2E tests...") }
    doLast { println("[OK] E2E tests completed.") }
}

tasks.register<Test>("testReservation") {
    group = "verification"
    description = "Run reservation tests (tests tagged with @reservation)"
    useJUnitPlatform()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }

    systemProperty("cucumber.filter.tags", "@reservation")
    environment(System.getenv())

    doFirst { println("[INFO] Running reservation tests...") }
    doLast { println("[OK] Reservation tests completed.") }
}

tasks.register<Test>("testPayment") {
    group = "verification"
    description = "Run payment tests (tests tagged with @payment)"
    useJUnitPlatform()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath
    filter { includeTestsMatching("com.camping.tests.RunCucumberTest") }

    systemProperty("cucumber.filter.tags", "@payment")
    environment(System.getenv())

    doFirst { println("[INFO] Running payment tests...") }
    doLast { println("[OK] Payment tests completed.") }
}
