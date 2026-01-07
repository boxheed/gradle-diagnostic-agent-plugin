package com.fizzpod.buildintelligence

import com.fizzpod.buildintelligence.dsl.BuildIntelligenceExtension
import com.fizzpod.buildintelligence.providers.MockProvider
import com.fizzpod.buildintelligence.services.BuildTelemetryService
import com.fizzpod.buildintelligence.spi.LlmProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.build.event.BuildEventsListenerRegistry

import javax.inject.Inject
import java.util.ServiceLoader

class BuildIntelligencePlugin implements Plugin<Project> {
    private final BuildEventsListenerRegistry registry

    @Inject
    BuildIntelligencePlugin(BuildEventsListenerRegistry registry) {
        this.registry = registry
    }

    void apply(Project project) {
        def extension = project.extensions.create("buildIntelligence", BuildIntelligenceExtension)

        // Set default convention
        extension.activeProvider.convention("mock")

        // Register known provider types so they can be created via DSL
        extension.providers.registerBinding(MockProvider.class, MockProvider.class)
        
        // Register default provider instance
        extension.providers.register("mock", MockProvider.class)

        // Use ServiceLoader to discover providers on the classpath
        def providerLoader = ServiceLoader.load(LlmProvider.class, getClass().getClassLoader())
        providerLoader.each { provider ->
            // Register the binding for the discovered provider class
            extension.providers.registerBinding(provider.getClass(), provider.getClass())

            // Register the discovered instance
            extension.providers.register(provider.getClass().getSimpleName().toLowerCase(), provider.getClass()) {
                // Here one could configure the provider instance if needed
            }
        }

        Provider<BuildTelemetryService> buildTelemetryServiceProvider = project.gradle.sharedServices.registerIfAbsent("buildTelemetry", BuildTelemetryService) { spec ->
            spec.parameters.enabled.set(extension.enabled)
            spec.parameters.dryRun.set(extension.dryRun)
            spec.parameters.activeProvider.set(extension.activeProvider)
            spec.parameters.gradleUserHomeDir.set(project.gradle.gradleUserHomeDir)
            spec.parameters.providers.set(project.provider { extension.providers.collectEntries { [it.name, it.getClass().name] } })
        }

        registry.onTaskCompletion(buildTelemetryServiceProvider)
    }
}
