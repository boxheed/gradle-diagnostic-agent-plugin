package com.sample

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response
import com.example.buildintelligence.spi.LlmProvider

class SampleProvider implements LlmProvider {
    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    Response analyze(Payload payload) {
        return new Response(
            analysis: "Analysis from the sample third-party provider!",
            providerId: "sample-provider",
            metadata: [:]
        )
    }
}
