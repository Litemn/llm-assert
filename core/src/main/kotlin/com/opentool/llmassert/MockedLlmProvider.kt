package com.opentool.llmassert


class MockedLlmProvider(
    var result: String = "true",
) : LlmProvider {
    override fun call(prompt: AssertPrompt): AssertCallResult {
        return AssertCallResult(result)
    }
}
