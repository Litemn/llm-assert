package com.opentool.llmassert

data class LlmAssertionConfig(
    val provider: LlmProvider,
    val defaultTemperature: Double = 0.0,
    val defaultSystemPrompt: String = "You are a helpful assistant that evaluates code assertions. " +
            "Respond ONLY with `true` if the assertion is correct, `false` if it's incorrect.",
    val parseRetryCount: Int = 1

) {
    init {
        require(parseRetryCount >= 1) { "parseRetryCount must be greater than or equal to 1" }
    }
}