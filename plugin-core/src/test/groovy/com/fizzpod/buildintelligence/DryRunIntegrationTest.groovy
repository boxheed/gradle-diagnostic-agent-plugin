package com.fizzpod.buildintelligence

import spock.lang.Unroll

class DryRunIntegrationTest extends BaseSpecification {

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
            
            buildIntelligence {
                dryRun = true
                // We don't need a real provider for this test
                activeProvider = "mock"
            }

            task failingTask {
                doLast {
                    throw new RuntimeException("This build is meant to fail!")
                }
            }
        """
    }

    @Unroll
    def "dryRun mode logs payload and does not run analysis"() {
        when:
        def result = runAndFail('failingTask')

        then:
        // Check for the dry run log message
        result.output.contains("[BuildIntelligence] DRY RUN: Payload that would be sent:")
        
        // Check that the analysis output is NOT present
        !result.output.contains("=== Build Intelligence Analysis ===")
    }
}
