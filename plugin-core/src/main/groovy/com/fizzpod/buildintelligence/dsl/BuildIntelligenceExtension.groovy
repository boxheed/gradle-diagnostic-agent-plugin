package com.fizzpod.buildintelligence.dsl

import com.fizzpod.buildintelligence.spi.LlmProvider
import org.gradle.api.Action
import org.gradle.api.PolymorphicDomainObjectContainer
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
    private final PolymorphicDomainObjectContainer<LlmProvider> providers

    @Inject
    BuildIntelligenceExtension(ObjectFactory objectFactory) {
        this.providers = objectFactory.polymorphicDomainObjectContainer(LlmProvider.class)
    }

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
    PolymorphicDomainObjectContainer<LlmProvider> getProviders() {
        return providers
    }

    /**
     * Configures the provider container.
     */
    void providers(Action<? super PolymorphicDomainObjectContainer<LlmProvider>> action) {
        action.execute(getProviders())
    }
}
