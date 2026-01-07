package com.example.buildintelligence.worker

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.spi.LlmProvider
import com.example.buildintelligence.reporting.ConsoleReporter
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

interface BuildAnalysisParameters extends WorkParameters {
    Property<Payload> getPayload()
    Property<String> getProviderClassName()
}

abstract class BuildAnalysisWorkAction implements WorkAction<BuildAnalysisParameters> {
    @Override
    void execute() {
        def payload = parameters.payload.get()
        def providerClassName = parameters.providerClassName.get()

        Class<?> providerClass = Class.forName(providerClassName)
        def provider = (LlmProvider) providerClass.newInstance()

        def response = provider.analyze(payload)
        def reporter = new ConsoleReporter()
        reporter.report(response)
    }
}
