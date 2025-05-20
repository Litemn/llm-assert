plugins {
    kotlin("jvm")
}

group = "com.open-tool"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://repo.spring.io/snapshot")
    maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
}

dependencies {
    implementation(project(":core"))
    implementation(platform("org.springframework.ai:spring-ai-bom:1.0.0-SNAPSHOT"))
    implementation("org.springframework.ai:spring-ai-client-chat")
    implementation("org.springframework.ai:spring-ai-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}