package com.opentool.llmassert

import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel

class SpringAiProvider(private val chatModel: ChatModel) : LlmProvider {
    override fun call(prompt: AssertPrompt): AssertCallResult {
        val messages: Array<Message> = arrayOf(
            SystemMessage(prompt.systemPrompt), UserMessage(prompt.assertPrompt)
        )
        val response: String? = chatModel.call(*messages)
        return AssertCallResult(response ?: throw IllegalStateException("No response from LLM"))
    }
}