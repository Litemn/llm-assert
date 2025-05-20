package com.opentool.llmassert

interface LlmAssertion {

    fun assert(prompt: String, media: Collection<Media> = emptyList()): AssertCallResult

    fun assertTrue(prompt: String, media: Collection<Media> = emptyList()) {
        val response = parseResult(assert(prompt, media))
        if (response is BooleanResult && !response.value) {
            throw AssertionError("Assertion failed: $prompt, LLM response: false")
        }
        if (response is Unparsed) {
            throw AssertionError("Fail to parse llm-assert: $prompt, LLM response: $response")
        }
    }

    fun assertFalse(prompt: String, media: Collection<Media> = emptyList()) {
        val response = parseResult(assert(prompt, media))
        if (response is BooleanResult && response.value) {
            throw AssertionError("Assertion failed: $prompt, LLM response: $response")
        }
        if (response is Unparsed) {
            throw AssertionError("Fail to parse llm-assert: $prompt, LLM response: $response")
        }
    }

    fun parseResult(response: AssertCallResult): ParseResult {
        val trim = response.text.trim()
        if (trim.equals("true", true) || trim.equals("false", true)) {
            return BooleanResult(trim.toBoolean())
        }
        return Unparsed(trim)
    }
}

sealed interface ParseResult

data class Unparsed(val text: String) : ParseResult

data class BooleanResult(val value: Boolean) : ParseResult