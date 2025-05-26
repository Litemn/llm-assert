package com.opentool.llmassert

// New Anthropic SDK imports
import com.anthropic.sdk.AnthropicClient
import com.anthropic.sdk.model.MessageParam
import com.anthropic.sdk.model.Role
import com.anthropic.sdk.model.ContentBlock
import com.anthropic.sdk.model.ClaudeModel // For specific model selection
import com.anthropic.sdk.model.MessageRequest // Main request object
import com.anthropic.sdk.exception.ApiErrorException
import com.anthropic.sdk.exception.AnthropicException

// Imports from core module (should remain)
import com.opentool.llmassert.LlmProvider
import com.opentool.llmassert.AssertPrompt
import com.opentool.llmassert.AssertCallResult
import com.opentool.llmassert.UserMessage
import com.opentool.llmassert.AssistantMessage
import com.opentool.llmassert.SystemMessage

class AnthropicLlmProvider(
    private val apiKey: String,
    private val client: AnthropicClient
) : LlmProvider {

    constructor(apiKey: String) : this(
        apiKey,
        AnthropicClient.builder()
            .apiKey(apiKey)
            .build()
    )

    private companion object {
        // Updated to use ClaudeModel enum if available, or a string if that's how the new SDK takes it.
        // The prompt mentioned `ClaudeModel`, so using an enum from it.
        // If the SDK builder expects a string, this would be ClaudeModel.CLAUDE_3_OPUS.toString() or specific string.
        // Assuming builder takes the enum or its string representation. For now, direct enum use.
        private val DEFAULT_MODEL = ClaudeModel.CLAUDE_3_OPUS // Example, adjust if SDK expects string.
        private const val DEFAULT_MAX_TOKENS = 1024
    }

    override fun call(prompt: AssertPrompt): AssertCallResult {
        if (apiKey.isBlank()) {
            return AssertCallResult("Error: API key is missing or blank.")
        }

        try {
            val sdkMessages = mutableListOf<MessageParam>()

            prompt.history.forEach { message ->
                when (message) {
                    is UserMessage -> {
                        sdkMessages.add(
                            MessageParam.builder()
                                .role(Role.USER)
                                .content(ContentBlock.text(message.text)) // As per prompt
                                .build()
                        )
                    }
                    is AssistantMessage -> {
                        sdkMessages.add(
                            MessageParam.builder()
                                .role(Role.ASSISTANT)
                                .content(ContentBlock.text(message.text)) // As per prompt
                                .build()
                        )
                    }
                    is SystemMessage -> { /* Explicitly ignore SystemMessage in history */ }
                }
            }

            sdkMessages.add(
                MessageParam.builder()
                    .role(Role.USER)
                    .content(ContentBlock.text(prompt.assertPrompt.text)) // As per prompt
                    .build()
            )

            val requestBuilder = MessageRequest.builder()
                .model(DEFAULT_MODEL) // Pass the ClaudeModel enum/string
                .messages(sdkMessages)
                .maxTokens(DEFAULT_MAX_TOKENS)

            prompt.systemPrompt.text.takeIf { it.isNotBlank() }?.let {
                requestBuilder.system(it)
            }

            val request = requestBuilder.build()

            // Assuming client.messages().create() is still the method with the new MessageRequest
            val response = this.client.messages().create(request)

            val responseText = response.content()
                .filterIsInstance<ContentBlock.Text>() // Get only text blocks
                .firstOrNull() // Take the first one
                ?.text() // Extract text from it
                ?: "" // If no text block or content is null, return empty string

            return AssertCallResult(responseText)

        } catch (e: ApiErrorException) {
            val errorDetails = e.message ?: "No further details"
            return AssertCallResult("Error: API call failed with status ${e.statusCode()}: $errorDetails")
        } catch (e: AnthropicException) {
            return AssertCallResult("Error: Anthropic SDK error - ${e.message ?: "No further details"}")
        } catch (e: Exception) {
            return AssertCallResult("Error: An unexpected error occurred - ${e.message ?: "No further details"}")
        }
    }
}
