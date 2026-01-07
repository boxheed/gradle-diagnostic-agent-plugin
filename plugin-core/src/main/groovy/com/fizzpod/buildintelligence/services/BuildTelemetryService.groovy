package com.fizzpod.buildintelligence.services

import com.fizzpod.buildintelligence.caching.FileBasedCache
import com.fizzpod.buildintelligence.model.Payload
import com.fizzpod.buildintelligence.reporting.ConsoleReporter
import com.fizzpod.buildintelligence.spi.LlmProvider
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

interface TelemetryParameters extends BuildServiceParameters {
    Property<Boolean> getEnabled()
    Property<Boolean> getDryRun()
    Property<String> getActiveProvider()
    DirectoryProperty getGradleUserHomeDir()
    MapProperty<String, String> getProviders()
}

abstract class BuildTelemetryService implements BuildService<TelemetryParameters>, OperationCompletionListener, AutoCloseable {

    private final ExecutorService executor = Executors.newSingleThreadExecutor()
    private final FileBasedCache cache
    private final org.gradle.api.logging.Logger logger = Logging.getLogger(BuildTelemetryService.class)
    private final ObjectFactory objectFactory

    @Inject
    BuildTelemetryService(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory
        this.cache = new FileBasedCache(parameters.gradleUserHomeDir.get().asFile)
    }

    @Override
    void onFinish(FinishEvent event) {
        if (!parameters.enabled.getOrElse(true)) {
            return
        }

        if (event instanceof TaskFinishEvent && event.result instanceof org.gradle.tooling.events.task.TaskFailureResult) {
            def failure = ((org.gradle.tooling.events.task.TaskFailureResult) event.result).failures[0]
            def rawLog = failure.message + "\n" + failure.description

            // Caching check
            def cacheKey = rawLog
            def cachedResponse = cache.get(cacheKey)
            if (cachedResponse != null) {
                logger.lifecycle("[BuildIntelligence] Found cached analysis.")
                new ConsoleReporter().report(cachedResponse)
                return
            }
            
            def privacyFilter = new PrivacyFilter()
            def contextTruncator = new ContextTruncator()
            
            def truncatedLog = contextTruncator.truncate(rawLog)
            def scrubbedLog = privacyFilter.scrub(truncatedLog)

            def payload = new Payload(
                buildId: UUID.randomUUID().toString(),
                failureLog: scrubbedLog,
                taskTimings: [:],
                environmentContext: [:]
            )

            if (parameters.dryRun.getOrElse(false)) {
                logger.lifecycle("[BuildIntelligence] DRY RUN: Payload that would be sent:")
                logger.lifecycle(payload.toString())
                return
            }

            def providerName = parameters.activeProvider.get()
            def providerClassName = parameters.providers.get().get(providerName)

            if (providerClassName == null) {
                logger.warn("[BuildIntelligence] Active provider '${providerName}' not found.")
                return
            }
            
            if (Boolean.getBoolean("buildintelligence.testing")) {
                runAnalysis(payload, providerClassName, providerName, cacheKey)
            } else {
                executor.submit {
                    runAnalysis(payload, providerClassName, providerName, cacheKey)
                }
            }
        }
    }

    private void runAnalysis(Payload payload, String providerClassName, String providerName, String cacheKey) {
        try {
            Class<?> providerClass = Class.forName(providerClassName)
            // Use ObjectFactory to create instance, allowing injection and name passing
            // We pass providerName assuming the constructor takes String name, or fallback to no-arg
            def provider
            try {
                provider = (LlmProvider) objectFactory.newInstance(providerClass, providerName)
            } catch (Exception e) {
                // Fallback to no-arg or other means if needed, but objectFactory should handle it
                 provider = (LlmProvider) objectFactory.newInstance(providerClass)
            }

            // Version Check
            if (provider.version != "1.0") {
                logger.warn("[BuildIntelligence] Provider '${providerName}' has incompatible version '${provider.version}'. Expected '1.0'.")
                return
            }
            
            def response = provider.analyze(payload)
            def reporter = new ConsoleReporter()
            reporter.report(response)

            // Cache the response
            cache.put(cacheKey, response)
        } catch (Exception e) {
            logger.error("Error running build intelligence analysis", e)
        }
    }

    @Override
    void close() throws Exception {
        executor.shutdown()
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow()
            }
        } catch (InterruptedException e) {
            executor.shutdownNow()
        }
    }
}
