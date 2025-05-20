package com.opentool.llmassert

import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.core.io.InputStreamResource
import org.springframework.util.MimeType
import org.springframework.ai.content.Media as SpringMedia

class SpringAiProvider(private val chatModel: ChatModel) : LlmProvider {

    override fun call(prompt: AssertPrompt): AssertCallResult {
        val userMessageBuilder = UserMessage.builder().text(prompt.assertPrompt.text)

        prompt.assertPrompt.media.forEach { media ->
            val springMedia = convertToSpringMedia(media)
            userMessageBuilder.media(springMedia)
        }

        val user = userMessageBuilder.build()
        val messages = mutableListOf(
            SystemMessage(prompt.systemPrompt.text), user
        )
        prompt.history.forEach { message ->
            when (message) {
                is com.opentool.llmassert.AssistantMessage -> messages.add(org.springframework.ai.chat.messages.AssistantMessage(message.text))
                is com.opentool.llmassert.SystemMessage -> messages.add(SystemMessage(message.text))
                is com.opentool.llmassert.UserMessage -> messages.add(UserMessage(message.text))
            }
        }
        val messagesArray = messages.toTypedArray()

        val response: String? = chatModel.call(*messagesArray)
        return AssertCallResult(response ?: throw IllegalStateException("No response from LLM"))
    }


    private fun convertToSpringMedia(media: com.opentool.llmassert.Media): SpringMedia? {
        val mimeType = MimeType.valueOf(media.mimeType)
        return SpringMedia(mimeType, InputStreamResource(media.source.invoke()))
    }
}
