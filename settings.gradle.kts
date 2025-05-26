plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "llm-assert"
include("core")
include("spring-ai")
include("anthropic")