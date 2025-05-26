package com.opentool.llmassert

// Anthropic SDK imports - new structure
import com.anthropic.sdk.AnthropicClient
import com.anthropic.sdk.service.Messages // Placeholder for the type returned by client.messages()
import com.anthropic.sdk.model.MessageRequest
import com.anthropic.sdk.model.MessageResponse
import com.anthropic.sdk.model.MessageParam // Used inside MessageRequest
import com.anthropic.sdk.model.ContentBlock
import com.anthropic.sdk.model.Role
import com.anthropic.sdk.model.ClaudeModel
import com.anthropic.sdk.exception.ApiErrorException
// import com.anthropic.sdk.exception.AnthropicException // Not explicitly used in these tests, but good to be aware of

// Classes from the same package (core module or this module)
import com.opentool.llmassert.AnthropicLlmProvider // Class under test
import com.opentool.llmassert.AssertPrompt
import com.opentool.llmassert.UserMessage
import com.opentool.llmassert.SystemMessage
// AssertCallResult is implicitly used through provider.call() return type

// MockK imports
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import io.mockk.mockk
import io.mockk.any

// JUnit imports
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnthropicLlmProviderTest {

    @MockK
    lateinit var mockAnthropicClient: AnthropicClient

    @MockK
    lateinit var mockMessages: Messages // Updated type for what client.messages() returns

    private lateinit var provider: AnthropicLlmProvider

    private val dummyApiKey = "test-api-key"
    private val dummyUserMessage = "User message here"
    private val dummySystemMessage = "System message here"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { mockAnthropicClient.messages() } returns mockMessages
    }

    @Test
    fun `call with blank API key returns error result`() {
        provider = AnthropicLlmProvider("", mockAnthropicClient)
        val prompt = AssertPrompt(UserMessage(dummyUserMessage), SystemMessage(dummySystemMessage))
        val result = provider.call(prompt)
        assertEquals("Error: API key is missing or blank.", result.text)
        verify(exactly = 0) { mockMessages.create(any()) }
    }

    @Test
    fun `call with valid API key and successful API response returns mapped result`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val prompt = AssertPrompt(UserMessage(dummyUserMessage), SystemMessage(dummySystemMessage))
        val expectedResponseText = "Expected LLM output"

        val mockApiResponse = mockk<MessageResponse>()
        val mockContentBlock = mockk<ContentBlock.Text>()
        
        every { mockContentBlock.text() } returns expectedResponseText
        every { mockApiResponse.content() } returns listOf(mockContentBlock) // Assuming content is a list

        every { mockMessages.create(any<MessageRequest>()) } returns mockApiResponse

        val result = provider.call(prompt)

        assertEquals(expectedResponseText, result.text)
        verify(exactly = 1) { mockMessages.create(any<MessageRequest>()) }
    }

    @Test
    fun `call when API throws ApiErrorException returns error result`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val prompt = AssertPrompt(UserMessage(dummyUserMessage), SystemMessage(dummySystemMessage))
        
        val apiException = mockk<ApiErrorException>(relaxed = true)
        every { apiException.statusCode() } returns 400 // Example status code
        every { apiException.message } returns "Mocked API Error"
        every { mockMessages.create(any<MessageRequest>()) } throws apiException

        val result = provider.call(prompt)
        
        assertTrue(result.text.contains("API call failed with status 400: Mocked API Error") || result.text.contains("Mocked API Error"))
    }

    @Test
    fun `call passes correct parameters to Anthropic SDK`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val userText = "Tell me a joke."
        val systemText = "Be a friendly assistant."
        val prompt = AssertPrompt(UserMessage(userText), SystemMessage(systemText))

        val requestSlot = slot<MessageRequest>()
        val mockApiResponse = mockk<MessageResponse>(relaxed = true)
        val mockContentBlock = mockk<ContentBlock.Text>(relaxed = true)
        every { mockContentBlock.text() } returns "some response"
        every { mockApiResponse.content() } returns listOf(mockContentBlock)

        every { mockMessages.create(capture(requestSlot)) } returns mockApiResponse

        provider.call(prompt)

        val capturedRequest = requestSlot.captured
        assertEquals(ClaudeModel.CLAUDE_3_OPUS, capturedRequest.model()) // Updated assertion for model
        assertEquals(1024, capturedRequest.maxTokens())
        assertEquals(systemText, capturedRequest.system())

        // Verify the messages within the captured request
        // The capturedRequest.messages() should be List<MessageParam>
        val userMessageInRequest = capturedRequest.messages().find { it.role() == Role.USER }
        assertNotNull(userMessageInRequest)
        // Content in MessageParam is a single ContentBlock now, not a list as per AnthropicLlmProvider change
        val userMessageContent = userMessageInRequest?.content() 
        assertTrue(userMessageContent is ContentBlock.Text)
        assertEquals(userText, (userMessageContent as ContentBlock.Text).text())
    }
}
