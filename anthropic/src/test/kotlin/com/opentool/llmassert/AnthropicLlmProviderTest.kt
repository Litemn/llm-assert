package com.opentool.llmassert

import com.anthropic.sdk.AnthropicClient
import com.anthropic.sdk.resources.MessagesResource
import com.anthropic.sdk.entities.messages.MessageParam
import com.anthropic.sdk.entities.messages.MessageResponse
import com.anthropic.sdk.entities.messages.ContentBlock
import com.anthropic.sdk.entities.messages.Role
import com.anthropic.sdk.errors.ApiErrorException // Using a common specific exception

// Classes from the same package (core module or this module)
import com.opentool.llmassert.AnthropicLlmProvider // Class under test
import com.opentool.llmassert.AssertPrompt
import com.opentool.llmassert.UserMessage
import com.opentool.llmassert.SystemMessage
// AssertCallResult is implicitly used through provider.call() return type

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension // For JUnit 5 integration if preferred, but BeforeEach works fine
import io.mockk.slot
import io.mockk.verify
import io.mockk.mockk

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertNotNull // Was missing
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class) // Alternative to manual MockKAnnotations.init(this)
class AnthropicLlmProviderTest {

    @MockK
    lateinit var mockAnthropicClient: AnthropicClient

    @MockK
    lateinit var mockMessagesResource: MessagesResource

    private lateinit var provider: AnthropicLlmProvider

    private val dummyApiKey = "test-api-key"
    private val dummyUserMessage = "User message here"
    private val dummySystemMessage = "System message here"

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this) // Initialize mocks

        // Common setup for mock client
        every { mockAnthropicClient.messages() } returns mockMessagesResource
    }

    @Test
    fun `call with blank API key returns error result`() {
        // Pass the mock client, though it won't be used for this specific logic path yet
        provider = AnthropicLlmProvider("", mockAnthropicClient)
        val prompt = AssertPrompt(
            UserMessage(dummyUserMessage),
            SystemMessage(dummySystemMessage)
        )

        val result = provider.call(prompt)

        assertEquals("Error: API key is missing or blank.", result.text)
        // Verify no interaction with the (mocked) client happened
        verify(exactly = 0) { mockMessagesResource.create(any()) }
    }

    @Test
    fun `call with valid API key and successful API response returns mapped result`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val prompt = AssertPrompt(
            UserMessage(dummyUserMessage),
            SystemMessage(dummySystemMessage)
        )
        val expectedResponseText = "Expected LLM output"

        val mockApiResponse = mockk<MessageResponse>()
        val mockContentBlock = mockk<ContentBlock.Text>() // Assuming text content
        
        every { mockContentBlock.text() } returns expectedResponseText
        // The SDK might return a list of ContentBlock, so we adapt
        every { mockApiResponse.content() } returns listOf(mockContentBlock)
        // every { mockApiResponse.stopReason() } returns "end_turn" // Optional: check stop reason if relevant

        every { mockMessagesResource.create(any()) } returns mockApiResponse

        val result = provider.call(prompt)

        assertEquals(expectedResponseText, result.text)
        verify(exactly = 1) { mockMessagesResource.create(any()) }
    }

    @Test
    fun `call when API throws ApiErrorException returns error result`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val prompt = AssertPrompt(
            UserMessage(dummyUserMessage),
            SystemMessage(dummySystemMessage)
        )

        val apiException = mockk<ApiErrorException>(relaxed = true) // relaxed mock for simplicity
        every { apiException.message } returns "Mocked API Error" // Ensure the exception has a message
        every { mockMessagesResource.create(any()) } throws apiException

        val result = provider.call(prompt)

        // This assertion depends on how AnthropicLlmProvider handles exceptions.
        // Assuming it catches it and returns a message in AssertCallResult.
        // This part of the test might need adjustment when AnthropicLlmProvider.call is implemented.
        assertTrue(result.text.contains("API call failed") || result.text.contains("Mocked API Error") || result.text.contains("ApiErrorException"))
    }

    @Test
    fun `call passes correct parameters to Anthropic SDK`() {
        provider = AnthropicLlmProvider(dummyApiKey, mockAnthropicClient)
        val userText = "Tell me a joke."
        val systemText = "Be a friendly assistant."
        val prompt = AssertPrompt(
            UserMessage(userText),
            SystemMessage(systemText)
        )

        val requestSlot = slot<MessageParam>()
        val mockApiResponse = mockk<MessageResponse>(relaxed = true) // Relaxed as we don't care about response here
        // Ensure content is not null and has a text block for the actual provider logic to work if it accesses it
        val mockContentBlock = mockk<ContentBlock.Text>(relaxed = true)
        every { mockContentBlock.text() } returns "some response"
        every { mockApiResponse.content() } returns listOf(mockContentBlock)


        every { mockMessagesResource.create(capture(requestSlot)) } returns mockApiResponse

        provider.call(prompt)

        val capturedRequest = requestSlot.captured
        // Assuming the actual implementation will use "claude-3-opus-20240229" and some max tokens
        // These will be set in the AnthropicLlmProvider implementation
        assertEquals("claude-3-opus-20240229", capturedRequest.model()) 
        assertEquals(1024, capturedRequest.maxTokens()) // Default expected max tokens
        assertEquals(systemText, capturedRequest.system())

        val userMessageInRequest = capturedRequest.messages().find { it.role() == Role.USER }
        assertNotNull(userMessageInRequest)
        val userMessageContent = userMessageInRequest?.content()?.firstOrNull()
        assertTrue(userMessageContent is ContentBlock.Text)
        assertEquals(userText, (userMessageContent as ContentBlock.Text).text())
    }
}

// Helper to get a non-null ContentBlock.Text from a MessageResponse, for cleaner mocking setup.
// Not directly used in these tests as the actual provider isn't fully implemented yet,
// but could be useful.
/*
fun mockSuccessfulTextResponse(text: String): MessageResponse {
    val mockResponse = mockk<MessageResponse>()
    val mockContentBlock = mockk<ContentBlock.Text>()
    every { mockContentBlock.text() } returns text
    every { mockResponse.content() } returns listOf(mockContentBlock)
    // every { mockResponse.stopReason() } returns "end_turn" // Or other relevant stop reasons
    return mockResponse
}
*/
