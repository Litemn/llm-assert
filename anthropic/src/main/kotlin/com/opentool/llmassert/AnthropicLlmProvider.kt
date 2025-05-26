package com.opentool.llmassert

import com.anthropic.sdk.AnthropicClient
import com.anthropic.sdk.entities.messages.MessageParam
import com.anthropic.sdk.entities.messages.Message // Renaming to avoid conflict with local Message
import com.anthropic.sdk.entities.messages.ContentBlock
import com.anthropic.sdk.entities.messages.Role
import com.anthropic.sdk.errors.AnthropicException
import com.anthropic.sdk.errors.ApiErrorException

// Imports from core module
import com.opentool.llmassert.LlmProvider
import com.opentool.llmassert.AssertPrompt
import com.opentool.llmassert.AssertCallResult
import com.opentool.llmassert.UserMessage
import com.opentool.llmassert.AssistantMessage
import com.opentool.llmassert.SystemMessage
// Note: com.opentool.llmassert.Message is the sealed interface,
// UserMessage, AssistantMessage, SystemMessage are its implementations.

class AnthropicLlmProvider(
    private val apiKey: String,
    private val client: AnthropicClient
) : LlmProvider {

    /**
     * Secondary constructor for convenience.
     * Initializes a default AnthropicClient if one is not provided.
     */
    constructor(apiKey: String) : this(
        apiKey,
        AnthropicClient.builder()
            .apiKey(apiKey)
            .build()
    )

    private companion object {
        private const val DEFAULT_MODEL = "claude-3-opus-20240229" // As used in tests
        private const val DEFAULT_MAX_TOKENS = 1024 // As used in tests
    }

    override fun call(prompt: AssertPrompt): AssertCallResult {
        if (apiKey.isBlank()) {
            return AssertCallResult("Error: API key is missing or blank.")
        }

        try {
            val sdkMessages = mutableListOf<com.anthropic.sdk.entities.messages.Message>()

            // Convert history messages
            prompt.history.forEach { message ->
                when (message) {
                    is UserMessage -> {
                        sdkMessages.add(
                            com.anthropic.sdk.entities.messages.Message.builder()
                                .role(Role.USER)
                                .content(listOf(ContentBlock.text(message.text)))
                                .build()
                        )
                    }
                    is AssistantMessage -> {
                        sdkMessages.add(
                            com.anthropic.sdk.entities.messages.Message.builder()
                                .role(Role.ASSISTANT)
                                .content(listOf(ContentBlock.text(message.text)))
                                .build()
                        )
                    }
                    // SystemMessage in history is not typical for Anthropic's message list,
                    // it's usually a top-level parameter. Ignoring if present in history.
                    is SystemMessage -> { /* Explicitly ignore */ }
                }
            }

            // Add the main assert prompt (current user message)
            sdkMessages.add(
                com.anthropic.sdk.entities.messages.Message.builder()
                    .role(Role.USER)
                    .content(listOf(ContentBlock.text(prompt.assertPrompt.text)))
                    .build()
            )

            val requestBuilder = MessageParam.builder()
                .model(DEFAULT_MODEL)
                .maxTokens(DEFAULT_MAX_TOKENS)
                .messages(sdkMessages)

            // Add system prompt if it's not blank
            prompt.systemPrompt.text.takeIf { it.isNotBlank() }?.let {
                requestBuilder.system(it)
            }

            val request = requestBuilder.build()

            val response = this.client.messages().create(request)

            // Extract text from the first text content block, if available
            val responseText = response.content()
                .filterIsInstance<ContentBlock.Text>()
                .firstOrNull()?.text()
                ?: "" // Return empty string if no text content or content is not text

            return AssertCallResult(responseText)

        } catch (e: ApiErrorException) {
            return AssertCallResult("Error: API call failed with status ${e.statusCode()}: ${e.message}")
        } catch (e: AnthropicException) {
            return AssertCallResult("Error: Anthropic SDK error - ${e.message}")
        } catch (e: Exception) {
            // Catch-all for other unexpected errors like network issues not caught by SDK
            return AssertCallResult("Error: An unexpected error occurred - ${e.message}")
        }
    }
}
