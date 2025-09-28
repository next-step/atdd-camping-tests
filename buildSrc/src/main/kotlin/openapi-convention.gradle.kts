// OpenAPI 자동 생성 태스크
tasks.register("generateOpenApiDocs") {
    group = "documentation"
    description = "Generate OpenAPI documentation from all services"

    dependsOn("composeUp")

    doLast {
        println("Waiting for services to be ready...")
        Thread.sleep(30000) // 서비스 시작 대기

        val openApiDir = file("docs/openapi/services")
        openApiDir.mkdirs()

        try {
            // Admin API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/openapi/services/admin.yaml",
                           "http://localhost:18082/v3/api-docs.yaml")
            }
            println("✓ Admin OpenAPI 문서 생성: docs/openapi/services/admin.yaml")

            // Kiosk API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/openapi/services/kiosk.yaml",
                           "http://localhost:18081/v3/api-docs.yaml")
            }
            println("✓ Kiosk OpenAPI 문서 생성: docs/openapi/services/kiosk.yaml")

            // Reservation API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/openapi/services/reservation.yaml",
                           "http://localhost:18083/v3/api-docs.yaml")
            }
            println("✓ Reservation OpenAPI 문서 생성: docs/openapi/services/reservation.yaml")

            println("\n🎉 모든 OpenAPI 문서가 성공적으로 생성되었습니다!")

        } catch (e: Exception) {
            println("❌ OpenAPI 문서 생성 중 오류 발생: ${e.message}")
            println("서비스가 정상적으로 실행되고 있는지 확인해주세요.")
        }
    }
}