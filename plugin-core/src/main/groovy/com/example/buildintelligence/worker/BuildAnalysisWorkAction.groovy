package com.example.buildintelligence.worker

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.providers.MockProvider
import com.example.buildintelligence.reporting.ConsoleReporter
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

interface BuildAnalysisParameters extends WorkParameters {
    Property<Payload> getPayload()
}

abstract class BuildAnalysisWorkAction implements WorkAction<BuildAnalysisParameters> {
    @Override
    void execute() {
        def payload = parameters.payload.get()
        def provider = new MockProvider()
        def response = provider.analyze(payload)
        def reporter = new ConsoleReporter()
        reporter.report(response)
    }
}
