# Tasks: BuildIntelligence Gradle Plugin

This document breaks down the implementation of the BuildIntelligence Gradle Plugin into actionable, dependency-ordered tasks.

**Implementation Strategy**: The feature will be built incrementally, organized by user stories. Each user story phase represents a testable, deliverable slice of functionality. The MVP is User Story 1.

## Phase 1: Project Setup
*Goal: Initialize the project structure and build configurations.*

- [ ] T001 Create a multi-module Gradle project with modules `:plugin-core` and `:plugin-providers` in `settings.gradle`.
- [ ] T002 Configure the `:plugin-core` module in `plugin-core/build.gradle` with the `groovy-gradle-plugin`.
- [ ] T003 Configure the `:plugin-providers` module in `plugin-providers/build.gradle`.
- [ ] T004 [P] Apply and configure the `com.github.johnrengelman.shadow` plugin in `plugin-providers/build.gradle`.
- [ ] T005 [P] Create source directories `plugin-core/src/main/groovy` and `plugin-providers/src/main/groovy`.

## Phase 2: Foundational Components
*Goal: Define the core interfaces and data structures required by all features.*

- [ ] T006 Define the `LlmProvider` SPI in `plugin-core/src/main/groovy/com/example/buildintelligence/spi/LlmProvider.groovy`.
- [ ] T007 [P] Define the `Payload` data class in `plugin-core/src/main/groovy/com/example/buildintelligence/model/Payload.groovy`.
- [ ] T008 [P] Define the `Response` data class in `plugin-core/src/main/groovy/com/example/buildintelligence/model/Response.groovy`.
- [ ] T009 Set up the base test infrastructure using `GradleRunner` in `plugin-core/src/test/groovy/com/example/buildintelligence/BaseSpecification.groovy`.

## Phase 3: User Story 1 - MVP Build Failure Analysis
*Goal: Implement the core end-to-end flow for analyzing a build failure with a hardcoded mock provider.*
*Independent Test: A failing build triggers an analysis and logs a mock response to the console.*

- [ ] T010 [US1] Implement `BuildTelemetryService` as a `BuildService` in `plugin-core/src/main/groovy/com/example/buildintelligence/services/BuildTelemetryService.groovy`.
- [ ] T011 [US1] Create the main plugin class and register `BuildTelemetryService` in `plugin-core/src/main/groovy/com/example/buildintelligence/BuildIntelligencePlugin.groovy`.
- [ ] T012 [P] [US1] Implement `PrivacyFilter` service for data scrubbing in `plugin-core/src/main/groovy/com/example/buildintelligence/services/PrivacyFilter.groovy`.
- [ ] T013 [P] [US1] Implement `ContextTruncator` service for managing token limits in `plugin-core/src/main/groovy/com/example/buildintelligence/services/ContextTruncator.groovy`.
- [ ] T014 [US1] Implement a non-configurable `MockProvider` in `plugin-core/src/main/groovy/com/example/buildintelligence/providers/MockProvider.groovy`.
- [ ] T015 [US1] Define `BuildAnalysisWorkAction` and its `WorkParameters` in `plugin-core/src/main/groovy/com/example/buildintelligence/worker/BuildAnalysisWorkAction.groovy`.
- [ ] T016 [US1] Integrate the services: `BuildTelemetryService` must capture failure data, pass it to the `ContextTruncator` and `PrivacyFilter`, and then submit the `BuildAnalysisWorkAction` with the `MockProvider`.
- [ ] T017 [P] [US1] Implement a basic console reporting service in `plugin-core/src/main/groovy/com/example/buildintelligence/reporting/ConsoleReporter.groovy`.
- [ ] T018 [US1] Create a `GradleRunner` test to verify that a failing build executes the full MVP flow in `plugin-core/src/test/groovy/com/example/buildintelligence/MvpFlowIntegrationTest.groovy`.

## Phase 4: User Story 2 - Configurable & Real Providers
*Goal: Introduce the user-facing DSL to configure and switch between multiple providers, including a real one.*
*Independent Test: A build can be configured via the DSL to use a real HTTP provider, and `dryRun` mode works as expected.*

- [ ] T019 [US2] Implement `BuildIntelligenceExtension` with `NamedDomainObjectContainer` and a `dryRun` property in `plugin-core/src/main/groovy/com/example/buildintelligence/dsl/BuildIntelligenceExtension.groovy`.
- [ ] T020 [US2] Register the `buildIntelligence` extension in the main `BuildIntelligencePlugin` class.
- [ ] T021 [US2] Implement `ValueSource` and related logic to securely provide credentials like API keys to providers, ensuring Configuration Cache compatibility.
- [ ] T022 [US2] Refactor `BuildTelemetryService` to use the provider configured in the `BuildIntelligenceExtension` instead of the hardcoded mock.
- [ ] T023 [US2] Implement the `dryRun` mode check in `BuildTelemetryService`.
- [ ] T024 [P] [US2] Implement a real `OpenAiProvider` using OkHttp in `plugin-providers/src/main/groovy/com/example/buildintelligence/providers/OpenAiProvider.groovy`.
- [ ] T025 [P] [US2] Create a `GradleRunner` test for the DSL, configuring multiple providers and verifying the correct one is chosen, in `plugin-core/src/test/groovy/com/example/buildintelligence/DslConfigurationIntegrationTest.groovy`.
- [ ] T026 [P] [US2] Create a `GradleRunner` test to verify `dryRun` mode logs the payload without making a network call in `plugin-core/src/test/groovy/com/example/buildintelligence/DryRunIntegrationTest.groovy`.

## Phase 5: User Story 3 - Extensible Provider Discovery
*Goal: Allow third parties to provide their own `LlmProvider` implementations.*
*Independent Test: A custom provider packaged in an external JAR can be discovered and used by the plugin.*

- [ ] T027 [US3] Refactor the provider loading mechanism in the main plugin class to use Java's `ServiceLoader` to discover `LlmProvider` implementations from the buildscript classpath.
- [ ] T028 [P] [US3] Create a minimal sample provider project to generate a third-party JAR for testing.
- [ ] T029 [US3] Create a `GradleRunner` test that adds the sample provider JAR to the buildscript classpath and asserts that it is discovered and executed in `plugin-core/src/test/groovy/com/example/buildintelligence/ProviderDiscoveryIntegrationTest.groovy`.
- [ ] T030 [P] [US3] Create documentation for third-party developers on how to implement and package a custom provider in `docs/extending.md`.

## Phase 6: Polish & Cross-Cutting Concerns
*Goal: Add production-ready features like caching, detailed reporting, and final documentation.*

- [ ] T031 [P] Implement the file-based caching service and integrate it into `BuildTelemetryService` in `plugin-core/src/main/groovy/com/example/buildintelligence/caching/`.
- [ ] T032 [P] Implement the full `BuildAnalysis.md` report generation task in `plugin-core/src/main/groovy/com/example/buildintelligence/reporting/`.
- [ ] T033 [P] Implement the `LlmProvider` version check logic.
- [ ] T034 [P] Implement exposure of observability metrics as defined in the spec.
- [ ] T035 Review and enhance all user-facing error messages and logging.
- [ ] T036 [P] Add Groovydoc to all public classes and methods.
- [ ] T037 Create the final `README.md` at the project root.

---
## Dependency Graph
The user stories are designed to be implemented sequentially, as each builds upon the last.

`Setup` → `Foundational` → `User Story 1 (MVP)` → `User Story 2 (Config)` → `User Story 3 (Extensible)` → `Polish`

## Parallel Execution
Within each phase, tasks marked with `[P]` can often be worked on in parallel. For example:
- In **Phase 3**, `PrivacyFilter` (T012), `ContextTruncator` (T013), and the console reporter (T017) can be developed simultaneously before being integrated.
- In **Phase 4**, implementing the `OpenAiProvider` (T024) can happen in parallel with the tests for the DSL (T025, T026).
