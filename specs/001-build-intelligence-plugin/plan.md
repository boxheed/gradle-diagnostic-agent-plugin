# Implementation Plan: BuildIntelligence Gradle Plugin

**Feature Spec**: [spec.md](./spec.md)
**Branch**: `001-build-intelligence-plugin`

## 1. Technical Context

This document outlines the technical implementation plan for the "BuildIntelligence" Gradle plugin.

-   **Technology Stack**: Groovy, Gradle Plugin Development Kit (PDK), Spock (for testing).
-   **Architecture**:
    -   A multi-module Gradle project (`:plugin-core`, `:plugin-providers`) to enforce separation of concerns.
    -   The `:plugin-providers` module will use the **ShadowJar** plugin to bundle its dependencies (like OkHttp), preventing classpath conflicts with projects that use the BuildIntelligence plugin.
-   **Key Gradle APIs**:
    -   **Lazy Configuration & Configuration Cache**: `Property` and `Provider` APIs are mandatory. To ensure full Configuration Cache compatibility, credentials and other external properties will be accessed via `ValueSource`.
    -   **`BuildService`**: To listen to build-wide events and collect telemetry.
    -   **`TaskExecutionListener`**: To gather fine-grained timing data.
    -   **Worker API**: For asynchronous execution of network-bound LLM calls.
    -   **`NamedDomainObjectContainer`**: For a flexible and idiomatic DSL for configuring multiple LLM providers.
-   **Dependencies**:
    -   `:plugin-core`: `gradleApi()`, `groovy-all`.
    -   `:plugin-providers`: `com.github.johnrengelman.shadow` (for ShadowJar), `OkHttp`.
-   **Testing**: `GradleRunner` from the TestKit will be used for all functional testing.

## 2. Constitution Check

This plan is validated against the project's [Constitution](../.specify/memory/constitution.md).

-   ✅ **I. Strict Type Safety & Immutability**: `ValueSource` and `Property` APIs enforce this.
-   ✅ **II. "Integration-First" Testing**: `GradleRunner` with a `MockProvider` adheres to the boundary strategy.
-   ✅ **III. User Experience & Consistency**: `dryRun` mode and clear error handling for API/versioning issues ensure a "Polite Application".
-   ✅ **IV. Clean Architecture & Separation of Concerns**: The multi-module structure with a `spi` separates core logic from provider implementations.
-   ✅ **V. Performance & Resource Hygiene**: Configuration Cache support, Worker API, and caching are all central to the design.
-   ✅ **VI. Dependency & Governance**: ShadowJar is used to manage dependency scope.

## 3. Phased Implementation

The project is broken down into the following phased milestones. Each phase delivers a testable vertical slice of functionality and references the detailed design artifacts.

### Phase 1: Core DSL and Extension
-   **Goal**: Establish the basic plugin structure and user-facing configuration DSL.
-   **Artifacts**:
    -   DSL Structure: See [`contracts/BuildIntelligenceDSL.groovy`](./contracts/BuildIntelligenceDSL.groovy)
    -   Data Model: See [`data-model.md#1-buildintelligenceextension`](./data-model.md#1-buildintelligenceextension)
-   **Tasks**:
    1.  Set up the multi-module Gradle project. Apply the `shadow` plugin to the `:plugin-providers` subproject.
    2.  Define the `BuildIntelligenceExtension` with lazy `Property` fields as specified in the data model, including a `dryRun` (Boolean) property.
    3.  Register the `buildIntelligence` extension, making the DSL available.
    4.  Implement `ValueSource` logic for retrieving credentials (e.g., API keys) from Gradle properties to ensure Configuration Cache compatibility.
-   **Test Plan**: `GradleRunner` test to verify the `dryRun` property can be set and the plugin configures correctly with and without the configuration cache enabled.

### Phase 2: Telemetry Collection
-   **Goal**: Collect build failure and timing information.
-   **Tasks**:
    1.  Create and register a `BuildTelemetryService` that implements `BuildEventsListener`.
    2.  Use the service's `onFinish` method to capture the build result.
    3.  Implement a `TaskExecutionListener` to record task timings.
-   **Test Plan**: `GradleRunner` test with a failing build to assert that the service captures the failure exception.

### Phase 3: The Privacy, Truncation, & Scrubbing Layer
-   **Goal**: Sanitize and manage the size of collected data.
-   **Artifacts**:
    -   Data Model: See [`data-model.md#3-privacyfilter--contexttruncator`](./data-model.md#3-privacyfilter--contexttruncator)
-   **Tasks**:
    1.  Define a `PrivacyFilter` service to scrub sensitive data using the defined redaction strategy.
    2.  **Implement a `ContextTruncator` service**. This service will prune large stack traces or logs to ensure the payload fits within LLM provider token limits.
    3.  Integrate both services into the `BuildTelemetryService`. Data will be first truncated, then scrubbed before being stored in the `Payload` object (see [`data-model.md#4-payload`](./data-model.md#4-payload)).
-   **Test Plan**: Unit tests for the `ContextTruncator` with large sample stack traces to verify the pruning logic.

### Phase 4: Pluggable Provider Interface
-   **Goal**: Define the provider abstraction and registration mechanism.
-   **Artifacts**:
    -   Interface Contract: See [`contracts/LlmProvider.groovy`](./contracts/LlmProvider.groovy)
    -   Data Model: See [`data-model.md#2-llmprovider`](./data-model.md#2-llmprovider)
-   **Tasks**:
    1.  Define the `LlmProvider` interface in `:plugin-core` as specified in the contract.
    2.  Use a `NamedDomainObjectContainer` in the extension to manage provider configurations.
    3.  When configuring providers, use `ValueSource` to pass credentials to the provider implementations.
    4.  Create a `MockProvider` for testing.
-   **Test Plan**: `GradleRunner` test to configure multiple providers and verify the correct one is selected.

### Phase 5: Concurrency & Analysis Execution
-   **Goal**: Execute the analysis off the main thread and respect `dryRun` mode.
-   **Tasks**:
    1.  Create a `BuildAnalysisWorkAction` using the Gradle Worker API.
    2.  In the `BuildTelemetryService`, check if `dryRun` mode is enabled.
    3.  **If `dryRun` is true**, log the final, redacted `Payload` that *would* be sent to the LLM, and do NOT submit the work action.
    4.  **If `dryRun` is false**, submit the `BuildAnalysisWorkAction` to a `WorkerExecutor`.
    5.  Implement a concrete provider in `:plugin-providers` that is bundled into a shadow JAR.
-   **Test Plan**:
    1.  A test with `dryRun = true` that executes a failing build and asserts that the redacted payload is logged and no network call is made.
    2.  A test with `dryRun = false` that asserts the `MockProvider`'s `analyze` method was called.

### Phase 6: Reporting & Caching
-   **Goal**: Display results and implement caching.
    -   Data Model: See [`data-model.md#6-buildanalysisreport`](./data-model.md#6-buildanalysisreport)
-   **Tasks**:
    1.  Implement ANSI color-coded console logging based on the `Response` object.
    2.  Create a task that generates the `BuildAnalysis.md` report.
    3.  Implement file-based caching based on a hash of the failure's stack trace.
-   **Test Plan**: Run a failing build twice; assert the second run is a cache hit and is faster. Verify the report content.