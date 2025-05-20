package com.opentool.llmassert

interface LlmAssertion {

    fun assert(prompt: String): AssertCallResult

    fun assertTrue(prompt: String) {
        val response = parseResult(assert(prompt))
        if (response is BooleanParsedResult && !response.value) {
            throw AssertionError("Assertion failed: $prompt, LLM response: false")
        }
        if (response is UnparsedResult) {
            throw AssertionError("Fail to parse llm-assert: $prompt, LLM response: $response")
        }
    }

    fun assertFalse(prompt: String) {
        val response = parseResult(assert(prompt))
        if (response is BooleanParsedResult && response.value) {
            throw AssertionError("Assertion failed: $prompt, LLM response: $response")
        }
        if (response is UnparsedResult) {
            throw AssertionError("Fail to parse llm-assert: $prompt, LLM response: $response")
        }
    }

    fun parseResult(response: AssertCallResult): ParsedResult {
        val trim = response.text.trim()
        if (trim.equals("true", true) || trim.equals("false", true)) {
            return BooleanParsedResult(trim.toBoolean())
        }
        return UnparsedResult(trim)
    }
}

interface ParsedResult

class UnparsedResult(val text: String) : ParsedResult

class BooleanParsedResult(val value: Boolean) : ParsedResult