package com.opentool.llmassert

import jdk.jfr.Enabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi


@EnabledIfEnvironmentVariable(
    named = "OPENAI_API_KEY",
    matches = ".*",
    disabledReason = "No OpenAI API key provided"
)
class SpringAiTest {

    @Test
    fun test() {
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
        assertion.assertTrue("5 is less than 10")
    }
}