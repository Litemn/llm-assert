package com.opentool.llmassert


class MockedLlmProvider(
    var result: String = "true",
    private val mediaAwareResult: ((prompt: String, media: Collection<Media>) -> String)? = null
) : LlmProvider {

    override fun call(prompt: AssertPrompt): AssertCallResult {
        val mediaResult = mediaAwareResult?.invoke(prompt.assertPrompt, prompt.media)

        return AssertCallResult(mediaResult ?: result)
    }
}
