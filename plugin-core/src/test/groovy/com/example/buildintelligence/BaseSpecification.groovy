package com.example.buildintelligence

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

class BaseSpecification extends Specification {
    @TempDir
    File testProjectDir

    def run(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(arguments)
                .withPluginClasspath()
                .build()
    }

    def runAndFail(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments(arguments)
                .withPluginClasspath()
                .buildAndFail()
    }
}
