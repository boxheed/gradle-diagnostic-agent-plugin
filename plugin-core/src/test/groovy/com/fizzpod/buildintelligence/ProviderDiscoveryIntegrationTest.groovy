package com.fizzpod.buildintelligence

import spock.lang.Unroll

class ProviderDiscoveryIntegrationTest extends BaseSpecification {

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
                // The SampleProvider class name is "SampleProvider", so it's registered as "sampleprovider"
                activeProvider = "sampleprovider"
            }

            task checkProvider {
                doLast {
                    def ext = project.extensions.getByType(com.fizzpod.buildintelligence.dsl.BuildIntelligenceExtension)
                    def provider = ext.providers.findByName("sampleprovider")
                    assert provider != null
                    println "Found provider: \${provider.name}"
                }
            }
        """
    }

    @Unroll
    def "discovers and configures provider from test classpath"() {
        when:
        def result = run('checkProvider', '--info') // use --info to see the println

        then:
        result.output.contains("Found provider: sampleprovider")
        result.output.contains("BUILD SUCCESSFUL")
    }
}
