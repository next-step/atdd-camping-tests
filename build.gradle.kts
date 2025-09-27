plugins {
    java
    id("infra-convention")
    id("app-convention")
    id("docker-compose-convention")
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
val jjwtVersion = "0.11.5"

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
    testImplementation("org.assertj:assertj-core:3.26.3")

    // JDBC driver for test hooks
    testImplementation("com.mysql:mysql-connector-j:8.3.0")

    // JWT (jjwt)
    implementation("io.jsonwebtoken:jjwt-api:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jjwtVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jjwtVersion}")

    testImplementation("org.slf4j:slf4j-api:2.0.13")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.14")
}

tasks.test {
    useJUnitPlatform()
}

// OpenAPI 자동 생성 태스크
tasks.register("generateOpenApiDocs") {
    group = "documentation"
    description = "Generate OpenAPI documentation from all services"

    dependsOn("composeUp")

    doLast {
        println("Waiting for services to be ready...")
        Thread.sleep(30000) // 서비스 시작 대기

        try {
            // Admin API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/admin-openapi.yaml",
                           "http://localhost:18082/v3/api-docs.yaml")
            }
            println("✓ Admin OpenAPI 문서 생성: docs/admin-openapi.yaml")

            // Kiosk API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/kiosk-openapi.yaml",
                           "http://localhost:18081/v3/api-docs.yaml")
            }
            println("✓ Kiosk OpenAPI 문서 생성: docs/kiosk-openapi.yaml")

            // Reservation API 문서 추출
            project.exec {
                commandLine("curl", "-o", "docs/reservation-openapi.yaml",
                           "http://localhost:18083/v3/api-docs.yaml")
            }
            println("✓ Reservation OpenAPI 문서 생성: docs/reservation-openapi.yaml")

            println("\n🎉 모든 OpenAPI 문서가 성공적으로 생성되었습니다!")

        } catch (e: Exception) {
            println("❌ OpenAPI 문서 생성 중 오류 발생: ${e.message}")
            println("서비스가 정상적으로 실행되고 있는지 확인해주세요.")
        }
    }
}
