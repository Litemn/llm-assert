package com.opentool.llmassert

interface LlmAssertion {

    fun assert(prompt: String): AssertCallResult

    fun assertTrue(prompt: String) {
        val response = assert(prompt).text.trim()
        if (!response.equals("true", true)) {
            throw AssertionError("Assertion failed: $prompt, LLM response: $response")
        }
    }
}