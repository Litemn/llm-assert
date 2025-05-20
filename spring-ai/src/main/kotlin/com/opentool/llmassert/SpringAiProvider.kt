package com.opentool.llmassert

import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.core.io.InputStreamResource
import org.springframework.util.MimeType
import org.springframework.ai.content.Media as SpringMedia

class SpringAiProvider(private val chatModel: ChatModel) : LlmProvider {

    override fun call(prompt: AssertPrompt): AssertCallResult {
        val userMessageBuilder = UserMessage.builder().text(prompt.assertPrompt)

        prompt.media.forEach { media ->
            val springMedia = convertToSpringMedia(media)
            userMessageBuilder.media(springMedia)
        }

        val user = userMessageBuilder.build()
        val messages: Array<Message> = arrayOf(
            SystemMessage(prompt.systemPrompt), user
        )

        val response: String? = chatModel.call(*messages)
        return AssertCallResult(response ?: throw IllegalStateException("No response from LLM"))
    }


    private fun convertToSpringMedia(media: com.opentool.llmassert.Media): SpringMedia? {
        val mimeType = MimeType.valueOf(media.mimeType)
        return SpringMedia(mimeType, InputStreamResource(media.source.invoke()))
    }
}
