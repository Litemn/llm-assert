package com.opentool.llmassert

class LlmAssertionImpl(private val config: LlmAssertionConfig) : LlmAssertion {

    override fun assert(prompt: String, media: Collection<Media>): AssertCallResult {
        val history = mutableListOf<Message>()
        var last: AssertCallResult? = null
        repeat(config.parseRetryCount) {
            val result =
                config.provider.call(
                    AssertPrompt(
                        UserMessage(prompt, media),
                        history = history,
                        systemPrompt = SystemMessage(config.defaultSystemPrompt),
                    )
                )
            last = result
            if (parseResult(result) is Unparsed) {
                history.add(AssistantMessage(result.text))
                history.add(UserMessage("Answer only `true` or `false`"))
            } else {
                return result
            }
        }
        return last ?: throw IllegalStateException("Failed performing LLM assertion $prompt")
    }
}
