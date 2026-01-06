# Research: HTTP Client for LLM Providers

**Date**: 2026-01-06

## Decision

We will use **OkHttp** as the primary HTTP client for the `:plugin-providers` module to communicate with LLM APIs.

## Rationale

The choice of an HTTP client is crucial for a plugin that relies on network communication. The client must be reliable, performant, and easy to use.

1.  **Modern, Fluent API**: OkHttp offers a clean, builder-based API that is intuitive and less verbose than traditional clients like Apache HttpClient. This improves code readability and maintainability.

2.  **Performance**: OkHttp is highly optimized for performance. It includes features like connection pooling, response caching, and transparent GZIP compression, which reduce latency and network overhead. For a plugin that could be run frequently, this efficiency is important.

3.  **Robustness & Resilience**: It includes built-in mechanisms for handling common network issues, such as automatic recovery from connection problems. This aligns with our goal of creating a robust plugin. The exponential backoff for rate limiting (FR-014) can be implemented cleanly using OkHttp's `Authenticator` or `Interceptor` features.

4.  **Ecosystem & Community**: OkHttp is a mature, widely adopted library, especially within the JVM ecosystem (including Android, where it is standard). It is actively maintained by Square and has a large community, ensuring continued support and a wealth of documentation and examples.

5.  **Synchronous & Asynchronous Support**: OkHttp provides straightforward APIs for both synchronous and asynchronous requests. While our primary use case within the Gradle Worker API might be synchronous, having a simple path to async operations is a valuable future-proofing feature.

## Alternatives Considered

-   **Apache HttpClient**: A very mature and feature-rich library. However, its API is generally considered more complex and verbose than OkHttp's. For the straightforward GET/POST requests we need to make to LLM APIs, its extensive feature set may be overkill.
-   **Java 11+ `java.net.http.HttpClient`**: Using the built-in Java HTTP client would avoid adding an external dependency. However, it lacks some of the advanced, out-of-the-box features of OkHttp, such as the sophisticated interceptor chain and connection recovery mechanisms. Relying on it would require more manual implementation for resilience features.

Given the balance of modern API design, performance features, and robustness, OkHttp is the most suitable choice for the `BuildIntelligence` plugin.
