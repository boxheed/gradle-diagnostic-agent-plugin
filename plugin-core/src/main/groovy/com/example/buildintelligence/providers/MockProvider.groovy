package com.example.buildintelligence.providers

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response
import com.example.buildintelligence.spi.LlmProvider

class MockProvider implements LlmProvider {
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
