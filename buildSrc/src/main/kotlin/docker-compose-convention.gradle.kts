// Convenience 태스크
tasks.register<Task>("composeUp") {
    group = "docker"
    description = "Start all services (infra + app)"
    dependsOn("infraUp", "appUp")
}

tasks.register<Task>("composeDown") {
    group = "docker"
    description = "Stop all services (app + infra)"
    dependsOn("appDown", "infraDown")
}

tasks.register("smokeTest") {
    group = "verification"
    description = "Run smoke tests"
    dependsOn("composeUp", "test")
    finalizedBy("composeDown")

    doLast {
        println("Smoke test completed for all services:")
        println("- Kiosk: http://localhost:18081")
        println("- Admin: http://localhost:18082")
        println("- Reservation: http://localhost:18083")
    }
}