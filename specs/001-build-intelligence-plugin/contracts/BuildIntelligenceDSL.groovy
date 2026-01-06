// This file is a representation of the target DSL for documentation and design purposes.

// Example build.gradle configuration
buildIntelligence {
    // Enable or disable the plugin's functionality
    enabled = true

    // Set the name of the provider configuration to use for analysis
    activeProvider = "openai"

    // Use dryRun to see the redacted data without making a real API call
    dryRun = false

    // Configure the different LLM providers in a container
    providers {
        // Define a provider named "openai"
        openai {
            // The API key should be sourced from a secure location like gradle.properties
            // and accessed via a ValueSource to be compatible with Configuration Caching.
            apiKey = providers.gradleProperty("OPENAI_API_KEY")
            endpoint = "https://api.openai.com/v1/chat/completions"
            model = "gpt-4-turbo"
        }

        // Define a provider for a local Ollama instance
        localOllama {
            endpoint = "http://localhost:11434/api/generate"
            model = "llama3"
        }
    }

    // Configure the privacy filter
    privacy {
        // Add custom regex patterns to scrub sensitive data
        redactionPatterns.add("my-secret-token-\w+")
    }

    // Configure timeouts
    timeout = Duration.ofSeconds(30)
}