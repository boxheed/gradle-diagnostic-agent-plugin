package com.example.buildintelligence.dsl

import com.example.buildintelligence.spi.LlmProvider
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

import javax.inject.Inject

/**
 * The main configuration block for the BuildIntelligence plugin.
 *
 * Example in `build.gradle`:
 * ```
 * buildIntelligence {
 *   enabled = true
 *   activeProvider = "openai"
 *   dryRun = false
 *   providers {
 *     openai(MyOpenAiProvider) {
 *       apiKey = "..."
 *     }
 *   }
 * }
 * ```
 */
abstract class BuildIntelligenceExtension {
    /**
     * Enables or disables the plugin. Default is `true`.
     */
    abstract Property<Boolean> getEnabled()

    /**
     * If true, the plugin will log the payload that would be sent to the LLM
     * instead of making a real API call. Useful for debugging. Default is `false`.
     */
    abstract Property<Boolean> getDryRun()

    /**
     * The name of the provider configuration to use for analysis.
     */
    abstract Property<String> getActiveProvider()

    /**
     * The container for all configured LLM providers.
     */
    abstract NamedDomainObjectContainer<LlmProvider> getProviders()

    /**
     * Configures the provider container.
     */
    void providers(Action<? super NamedDomainObjectContainer<LlmProvider>> action) {
        action.execute(getProviders())
    }
}
