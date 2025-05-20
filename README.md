# LLM-Assert

LLM-Assert is a Kotlin library that enables making assertions using Large Language Models (LLMs). It provides a simple API for verifying conditions using natural language prompts, with support for both text and media inputs.

## Features

- Make assertions using natural language prompts
- Support for media attachments (images)
- Integration with Spring AI for using with different LLMs provider
- Configurable system prompts and temperature settings
- Mocked provider for testing without LLM dependencies

## Installation

### Gradle (Kotlin DSL)

```kotlin
TODO
```

## Usage

### Basic Usage

```kotlin
// Create a provider (using Spring AI with OpenAI)
val openAiApi = OpenAiApi.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .build()

val openAiChatOptions = OpenAiChatOptions.builder()
    .model("gpt-4o-mini")
    .temperature(0.0)
    .build()

val chatModel = OpenAiChatModel.builder()
    .openAiApi(openAiApi)
    .defaultOptions(openAiChatOptions)
    .build()

val provider = SpringAiProvider(chatModel)

// Create the assertion instance
val assertion: LlmAssertion = LlmAssertionImpl(
    config = LlmAssertionConfig(provider)
)

// Make assertions
assertion.assertTrue("2 + 2 = 4")
assertion.assertFalse("2 + 2 = 5")
```

### With Media Attachments

```kotlin
// Create an image media attachment
val imageMedia = Media.png(Path("path/to/image.png"))

// Make an assertion with the image
assertion.assertTrue("The image contains a cat", listOf(imageMedia))
```

### Testing with Mocked Provider

```kotlin
// Create a mocked provider for testing
val provider = MockedLlmProvider(result = "true")

// Create the assertion instance
val assertion: LlmAssertion = LlmAssertionImpl(
    config = LlmAssertionConfig(provider)
)

// Make assertions without calling an actual LLM
assertion.assertTrue("5 is a digit")
```

## Configuration

The library can be configured using the `LlmAssertionConfig` class:

```kotlin
val config = LlmAssertionConfig(
    provider = yourProvider,
    defaultTemperature = 0.0,  // Default is 0.0 for deterministic responses
    defaultSystemPrompt = "Custom system prompt"  // Override the default system prompt
)

val assertion = LlmAssertionImpl(config)
```

## API Reference

### LlmAssertion

The main interface for making assertions:

- `assertTrue(prompt: String, media: Collection<Media> = emptyList())` - Asserts that the LLM response is true
- `assertFalse(prompt: String, media: Collection<Media> = emptyList())` - Asserts that the LLM response is false

### Media

Class for representing media attachments:

- `Media.png(path: Path)` - Create a PNG image attachment
- `Media.jpeg(path: Path)` - Create a JPEG image attachment

### LlmProvider

Interface for LLM providers:

- `call(prompt: AssertPrompt): AssertCallResult` - Call the LLM with the given prompt

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
