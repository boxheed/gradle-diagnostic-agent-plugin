package com.example.buildintelligence.services

import com.example.buildintelligence.caching.FileBasedCache
import com.example.buildintelligence.dsl.BuildIntelligenceExtension
import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.reporting.ConsoleReporter
import com.example.buildintelligence.spi.LlmProvider
import com.example.buildintelligence.worker.BuildAnalysisWorkAction
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Input
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

interface TelemetryParameters extends BuildServiceParameters {
    @Input
    Project getProject()
}

abstract class BuildTelemetryService implements BuildService<TelemetryParameters>, OperationCompletionListener {

    private final WorkerExecutor workerExecutor
    private final Project project
    private final FileBasedCache cache

    @Inject
    BuildTelemetryService(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor
        this.project = parameters.project
        this.cache = new FileBasedCache(project.gradle.gradleUserHomeDir)
    }

    @Override
    void onFinish(FinishEvent event) {
        def extension = project.extensions.getByType(BuildIntelligenceExtension)
        if (!extension.enabled.getOrElse(true)) {
            return
        }

        if (event instanceof TaskFinishEvent && event.result instanceof org.gradle.tooling.events.task.TaskFailureResult) {
            def failure = ((org.gradle.tooling.events.task.TaskFailureResult) event.result).failures[0]
            def rawLog = failure.message + "\n" + failure.description

            // Caching check
            def cacheKey = rawLog
            def cachedResponse = cache.get(cacheKey)
            if (cachedResponse != null) {
                project.logger.lifecycle("[BuildIntelligence] Found cached analysis.")
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

            if (extension.dryRun.getOrElse(false)) {
                project.logger.lifecycle("[BuildIntelligence] DRY RUN: Payload that would be sent:")
                project.logger.lifecycle(payload.toString())
                return
            }

            def providerName = extension.activeProvider.get()
            def provider = extension.providers.findByName(providerName)
            if (provider == null) {
                project.logger.warn("[BuildIntelligence] Active provider '${providerName}' not found.")
                return
            }
            
            // Version Check
            if (provider.version != "1.0") {
                project.logger.warn("[BuildIntelligence] Provider '${providerName}' has incompatible version '${provider.version}'. Expected '1.0'.")
                return
            }
            
            // This part will need more refactoring to pass provider info to the worker
            // and to handle the response for caching.
            def workQueue = workerExecutor.noIsolation()
            workQueue.submit(BuildAnalysisWorkAction) { parameters ->
                parameters.payload.set(payload)
            }
        }
    }
}
