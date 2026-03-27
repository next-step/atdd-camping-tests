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

}

tasks.test {
    useJUnitPlatform()
    // -Dcucumber.filter.tags 는 Gradle JVM에 설정되므로 테스트 JVM으로 명시적으로 전달
    val tagFilter = System.getProperty("cucumber.filter.tags")
    if (tagFilter != null) systemProperty("cucumber.filter.tags", tagFilter)
}

tasks.register<Test>("smokeTest") {
    group = "verification"
    description = "@smoke 테스트 실행 (컨테이너 기동/종료는 SmokeHooks 자동 처리)"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@smoke")
}

tasks.register<Test>("e2eTest") {
    group = "verification"
    description = "@e2e 테스트 실행 (E2E 시나리오)"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@e2e")
}

tasks.register<Test>("allTest") {
    group = "verification"
    description = "@smoke + @e2e 테스트 전체 실행"
    useJUnitPlatform()
    systemProperty("cucumber.filter.tags", "@smoke or @e2e")
}
