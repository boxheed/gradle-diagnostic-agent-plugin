# BuildIntelligence Gradle Plugin

[![CI Status](https://img.shields.io/badge/ci-passing-brightgreen)](#)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](./LICENSE)

The **BuildIntelligence Gradle Plugin** automatically analyzes your build failures using the power of Large Language Models (LLMs), providing you with instant root cause analysis and suggested fixes directly in your console.

Stop deciphering cryptic stack traces and let AI do the heavy lifting.

## Features

-   **Automatic Failure Analysis**: On any build failure, the plugin captures the context and sends it to an LLM for analysis.
-   **Pluggable AI Providers**: Configure multiple LLM providers (e.g., OpenAI, Ollama, or internal services) and switch between them.
-   **Privacy-Focused**: Your data is sanitized before it leaves your machine. Sensitive information like file paths, environment variables, and secrets are scrubbed.
-   **Configuration as Code**: A clean, idiomatic Groovy DSL allows for easy and powerful configuration within your `build.gradle` file.
-   **Extensible**: A simple Service Provider Interface (SPI) allows developers to create and share their own providers.
-   **Caching**: Avoids redundant API calls for repeated failures, saving time and money.
-   **Dry Run Mode**: Preview the data that would be sent to the LLM without making a real API call.

## Quickstart

See the [Quickstart Guide](./specs/001-build-intelligence-plugin/quickstart.md) to get started in minutes.

## Documentation

-   [Feature Specification](./specs/001-build-intelligence-plugin/spec.md)
-   [Implementation Plan](./specs/001-build-intelligence-plugin/plan.md)
-   [Extending the Plugin](./docs/extending.md)

## Development

This project is built with Gradle.

-   To build the plugin: `./gradlew build`
-   To run the tests: `./gradlew test`

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](./LICENSE) file for details.
