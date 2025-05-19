package com.opentool.llmassert

data class LlmAssertionConfig(
    val provider: LlmProvider,
    val defaultTemperature: Double = 0.0,
    val defaultSystemPrompt: String = "You are a helpful assistant that evaluates code assertions. " +
            "Respond with 'true' if the assertion is correct, 'false' if it's incorrect."
)