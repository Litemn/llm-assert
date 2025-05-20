package com.opentool.llmassert

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import kotlin.io.path.Path


@EnabledIfEnvironmentVariable(
    named = "OPENAI_API_KEY",
    matches = ".*",
    disabledReason = "No OpenAI API key provided"
)
class SpringAiTest {

    @Test
    fun testWithoutMedia() {
        val openAiApi = OpenAiApi.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        val openAiChatOptions = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.0)
            .build()

        val chatModel: OpenAiChatModel =
            OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build()
        val provider = SpringAiProvider(chatModel)
        val assertion: LlmAssertion = LlmAssertionImpl(config = LlmAssertionConfig(provider))

        assertion.assertTrue("2 + 2 = 4")
    }

    @Test
    fun testWithImageMedia() {
        val openAiApi = OpenAiApi.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .build();
        val openAiChatOptions = OpenAiChatOptions.builder()
            .model("gpt-4o-mini")
            .temperature(0.0)
            .build()

        val chatModel: OpenAiChatModel =
            OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build()
        val provider = SpringAiProvider(chatModel)
        val assertion: LlmAssertion = LlmAssertionImpl(config = LlmAssertionConfig(provider))

        val imageMedia = Media.png(Path("src/test/resources/test.png"))

        assertion.assertTrue("All text on image in english language", listOf(imageMedia))
    }
}