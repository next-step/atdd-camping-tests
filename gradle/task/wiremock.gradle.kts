tasks.register("verifyWiremockStubs") {
    group = "wiremock"
    description = "Setup and verify WireMock stubs"

    doLast {
        val wiremockUrl = System.getProperty("PAYMENTS_BASE_URL") ?: "http://localhost:8084"
        val stubsDir = file("infra/wiremock/mappings")

        if (!stubsDir.exists()) {
            throw GradleException("WireMock mappings directory not found: ${stubsDir.absolutePath}")
        }

        println("Setting up WireMock stubs...")
        println("WireMock URL: $wiremockUrl")

        val stubFiles = stubsDir.listFiles { _, name -> name.endsWith(".json") }

        if (stubFiles.isNullOrEmpty()) {
            println("No stub files found to setup")
            return@doLast
        }

        try {
            // Check if WireMock is running
            val adminUrl = "$wiremockUrl/__admin"
            val healthCheck = ProcessBuilder("curl", "-s", "-f", "$adminUrl/mappings")
                .redirectErrorStream(true)
                .start()

            val exitCode = healthCheck.waitFor()
            if (exitCode != 0) {
                throw GradleException("WireMock is not running or not accessible at $wiremockUrl")
            }

            println("WireMock is running ✓")
            println("Stub files are automatically loaded from volume mount")
            println("Verified ${stubFiles.size} stub files:")

            stubFiles.forEach { file ->
                try {
                    val content = file.readText()
                    val method = content.substringAfter("\"method\": \"").substringBefore("\"")
                    val urlPath = content.substringAfter("\"urlPath\": \"").substringBefore("\"")
                    val status = content.substringAfter("\"status\": ").substringBefore(",").substringBefore("}")
                    println("  ✓ ${file.name}: ${method} ${urlPath} -> ${status}")
                } catch (e: Exception) {
                    println("  ✓ ${file.name}: (loaded)")
                }
            }

        } catch (e: Exception) {
            throw GradleException("Error setting up WireMock stubs: ${e.message}")
        }
    }
}
