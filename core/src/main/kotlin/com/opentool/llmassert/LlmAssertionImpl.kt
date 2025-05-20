package com.opentool.llmassert

class LlmAssertionImpl(private val config: LlmAssertionConfig) : LlmAssertion {

    override fun assert(prompt: String, media: Collection<Media>): AssertCallResult {
        return config.provider.call(AssertPrompt(prompt, systemPrompt = config.defaultSystemPrompt, media = media))
    }
}
