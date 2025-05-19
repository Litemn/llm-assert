package com.opentool.llmassert

import org.junit.jupiter.api.Test

class SimpleTest {

    @Test
    fun simpleTest() {
        val provider = MockedLlmProvider()
        val assertion: LlmAssertion = LlmAssertionImpl(config = LlmAssertionConfig(provider))
        assertion.assertTrue("5 is a digit")
    }
}