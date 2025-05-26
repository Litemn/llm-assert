plugins {
    kotlin("jvm")
}

group = "com.open-tool"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // For Kotlin standard library
    implementation(kotlin("stdlib-jdk8"))

    // API dependency on the core module to access LlmProvider interface etc.
    api(project(":core"))

    // Anthropic SDK
    implementation("com.anthropic:anthropic-sdk-java:0.5.0")

    // Basic Kotlin testing utilities
    testImplementation(kotlin("test"))

    // MockK for mocking in tests
    testImplementation("io.mockk:mockk:1.13.10")

    // Explicit JUnit 5 support for Kotlin tests
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
