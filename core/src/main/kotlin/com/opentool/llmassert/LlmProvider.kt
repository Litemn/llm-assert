package com.opentool.llmassert

interface LlmProvider {
    fun call(prompt: AssertPrompt): AssertCallResult
}

data class AssertPrompt(val assertPrompt: String, val systemPrompt: String)

data class AssertCallResult(val text: String)
