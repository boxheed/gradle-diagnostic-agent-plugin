package com.fizzpod.buildintelligence.reporting
import com.fizzpod.buildintelligence.model.Response
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class BuildAnalysisReportTask extends DefaultTask {

    @Input
    abstract Property<Response> getAnalysisResponse()

    @OutputFile
    abstract Property<File> getOutputFile()

    @TaskAction
    void generateReport() {
        def response = analysisResponse.get()
        def reportFile = outputFile.get()

        reportFile.text = """
# Build Intelligence Analysis Report

**Provider**: ${response.providerId}
**Model**: ${response.metadata.get('model', 'N/A')}

---

## Analysis

${response.analysis}
"""
    }
}
