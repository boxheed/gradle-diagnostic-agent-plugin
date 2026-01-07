package com.example.buildintelligence.providers

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response
import com.example.buildintelligence.spi.LlmProvider

import javax.inject.Inject

class MockProvider implements LlmProvider {
    private final String name

    @Inject
    MockProvider(String name) {
        this.name = name
    }

    MockProvider() {
        this.name = "mock"
    }

    @Override
    String getName() {
        return name
    }

    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    Response analyze(Payload payload) {
        return new Response(
            analysis: "This is a mock analysis for the build failure.",
            providerId: "mock-provider",
            metadata: [:]
        )
    }
}
