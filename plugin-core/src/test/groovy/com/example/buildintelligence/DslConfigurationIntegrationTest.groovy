package com.example.buildintelligence

import spock.lang.Unroll

class DslConfigurationIntegrationTest extends BaseSpecification {

    def setup() {
        def settingsFile = new File(testProjectDir, 'settings.gradle')
        def buildFile = new File(testProjectDir, 'build.gradle')
        settingsFile.text = """
            rootProject.name = 'test-project'
        """
        buildFile.text = """
            plugins {
                id 'com.example.build-intelligence'
            }
            
            // A simple provider for testing configuration
            import javax.inject.Inject
            class TestProvider implements com.example.buildintelligence.spi.LlmProvider {
                private final String name

                @Inject
                TestProvider(String name) {
                    this.name = name
                }

                String getName() { return name }

                String version = "1.0"
                com.example.buildintelligence.model.Response analyze(com.example.buildintelligence.model.Payload p) { null }
            }

            buildIntelligence {
                enabled = true
                activeProvider = "test"
                providers {
                    registerBinding(TestProvider, TestProvider)
                    create("test", TestProvider) {
                        // no params needed
                    }
                }
            }
            
            task checkConfig {
                doLast {
                    def ext = project.extensions.getByType(com.example.buildintelligence.dsl.BuildIntelligenceExtension)
                    assert ext.activeProvider.get() == "test"
                    assert ext.providers.findByName("test") != null
                }
            }
        """
    }

    @Unroll
    def "DSL configuration is applied correctly"() {
        when:
        def result = run('checkConfig')

        then:
        result.output.contains("BUILD SUCCESSFUL")
    }
}