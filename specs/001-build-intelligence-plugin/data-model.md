# Data Model: BuildIntelligence Plugin

**Date**: 2026-01-06
**Based on**: [spec.md](./spec.md)

This document outlines the key data entities for the BuildIntelligence Gradle Plugin. These definitions are technology-agnostic and focus on the data structure and relationships.

---

### 1. BuildIntelligenceExtension
Represents the user-configurable DSL block (`buildIntelligence { ... }`) in a Gradle build script. It is the main entry point for all plugin settings.

-   **Fields**:
    -   `enabled` (Boolean): A property to enable or disable the plugin's functionality.
    -   `activeProvider` (String): A property holding the name of the provider to be used for analysis.
    -   `dryRun` (Boolean): A property to enable "dry run" mode. If true, the plugin will log the redacted data it would send to the LLM but will not make an actual API call.
    -   `providers` (Container<LlmProvider>): A container of all configured LLM provider profiles.
    -   `logSizeLimit` (Integer): A property to configure the maximum size of logs to be collected.
    -   `timeout` (Duration): A property to configure the network timeout for LLM API calls.
-   **Relationships**:
    -   Has a one-to-many relationship with `LlmProvider` configurations.

---

### 2. LlmProvider
An interface that defines the contract for a pluggable analysis provider. Each provider configuration corresponds to a specific LLM service.

-   **Fields (as configuration, not on the interface itself)**:
    -   `name` (String, implicit): The unique name of the provider configuration (e.g., 'gpt4', 'localOllama').
    -   `apiKey` (String): The API key for the LLM service. This should be handled securely.
    -   `endpoint` (String): The base URL for the LLM service API.
-   **Interface Methods**:
    -   `analyze(Payload p)`: The core method that takes a `Payload` and returns a `Response`.
    -   `getVersion()`: Returns the version of the provider interface it implements.
-   **Validation**: The `name` must be unique within the `providers` container. `apiKey` and `endpoint` are required for a valid configuration.

---

### 3. PrivacyFilter & ContextTruncator
These are internal services responsible for sanitizing and managing the size of data before it is sent to a provider.

-   **Interface Methods**:
    -   `scrub(String content)`: Takes a string and returns a sanitized version.
    -   `truncate(String content)`: Takes a string and returns a pruned version that respects token limits.
-   **Behavior**:
    -   The `PrivacyFilter` uses regex and high-entropy detection to remove sensitive data.
    -   The `ContextTruncator` intelligently shortens logs or stack traces to a configurable limit.

---

### 4. Payload
An immutable data object containing all the necessary information about a build failure, which is sent to an `LlmProvider` for analysis.

-   **Fields**:
    -   `buildId` (String): A unique identifier for the build.
    -   `failureLog` (String): The sanitized and truncated log output from the build.
    -   `taskTimings` (Map<String, Long>): A map of task paths to their execution duration in milliseconds.
    -   `environmentContext` (Map<String, String>): A minimal, safe set of environment details (e.g., OS, Java version).
-   **State**: The `Payload` is created once the build has failed and all data has been collected, truncated, and sanitized. It is immutable.

---

### 5. Response
An immutable data object containing the analysis result received from an `LlmProvider`.

-   **Fields**:
    -   `analysis` (String): The human-readable analysis of the build failure, provided by the LLM.
    -   `providerId` (String): The name of the provider that generated the response.
    -   `metadata` (Map<String, String>): Additional metadata, such as the LLM model used (e.g., 'gpt-4-turbo').

---

### 6. BuildAnalysisReport
Represents the final output that is presented to the user, both on the console and in a persistent file.

-   **Fields**:
    -   `title` (String): The title of the report.
    -   `analysisContent` (String): The core analysis from the `Response`.
    -   `buildSummary` (Map<String, String>): A summary of the build context (e.g., failed tasks, duration).
-   **Behavior**:
    -   Can be rendered as ANSI-colored text for console output.
    -   Can be rendered as Markdown for the `build/reports/BuildAnalysis.md` file.