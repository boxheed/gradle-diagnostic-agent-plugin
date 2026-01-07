package com.example.buildintelligence

import com.example.buildintelligence.dsl.BuildIntelligenceExtension
import com.example.buildintelligence.services.BuildTelemetryService
import com.example.buildintelligence.spi.LlmProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import java.util.ServiceLoader

class BuildIntelligencePlugin implements Plugin<Project> {
    void apply(Project project) {
        def extension = project.extensions.create("buildIntelligence", BuildIntelligenceExtension)
        extension.providers = project.container(LlmProvider.class)
        
        // Use ServiceLoader to discover providers on the classpath
        def providerLoader = ServiceLoader.load(LlmProvider.class, getClass().getClassLoader())
        providerLoader.each { provider ->
            extension.providers.register(provider.getClass().getSimpleName().toLowerCase(), provider.getClass()) {
                // Here one could configure the provider instance if needed
            }
        }

        Provider<BuildTelemetryService> buildTelemetryServiceProvider = project.gradle.sharedServices.registerIfAbsent("buildTelemetry", BuildTelemetryService) { spec ->
            spec.parameters.project = project
        }
    }
}
