package com.opentool.llmassert

interface LlmProvider {
    fun call(prompt: AssertPrompt): AssertCallResult
}

data class AssertPrompt(
    val assertPrompt: UserMessage,
    val systemPrompt: SystemMessage,
    val history: List<Message> = emptyList(),
)

sealed interface Message

data class UserMessage(val text: String, val media: Collection<Media> = emptyList()) : Message
data class SystemMessage(val text: String) : Message
data class AssistantMessage(val text: String) : Message


data class AssertCallResult(val text: String)
