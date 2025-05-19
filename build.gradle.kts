plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.open-tool"
version = "0.0.1"

repositories {
    mavenCentral()
    maven(url = "https://packages.jetbrains.team/maven/p/grazi/grazie-platform-public")
}
val grazieVersion = "0.4.42"

dependencies {
    implementation("ai.grazie.api:api-gateway-client:${grazieVersion}")
    implementation("ai.grazie.client:client-ktor:${grazieVersion}")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}