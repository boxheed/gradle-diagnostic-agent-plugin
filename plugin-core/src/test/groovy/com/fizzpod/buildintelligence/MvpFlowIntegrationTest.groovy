package com.fizzpod.buildintelligence

import spock.lang.Unroll

class MvpFlowIntegrationTest extends BaseSpecification {

    def setup() {
        def settingsFile = new File(testProjectDir, 'settings.gradle')
        def buildFile = new File(testProjectDir, 'build.gradle')
        settingsFile.text = """
            rootProject.name = 'test-project'
        """
        buildFile.text = """
            plugins {
                id 'com.fizzpod.build-intelligence'
            }
            
            task failingTask {
                doLast {
                    throw new RuntimeException("This build is meant to fail!")
                }
            }
        """
    }

    @Unroll
    def "failing build triggers mock analysis"() {
        when:
        def result = runAndFail('failingTask', '-Dbuildintelligence.testing=true')

        then:
        result.output.contains("=== Build Intelligence Analysis ===")
        result.output.contains("Provider: mock-provider")
        result.output.contains("This is a mock analysis for the build failure.")
    }
}
