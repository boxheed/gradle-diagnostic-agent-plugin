package com.example.buildintelligence.spi

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response

/**
 * Service Provider Interface (SPI) for a pluggable Large Language Model (LLM) provider.
 *
 * Implementations of this interface can be discovered by the BuildIntelligence plugin
 * to provide analysis for build failures.
 */
interface LlmProvider {

    /**
     * The version of the SPI this provider implements. This is used by the plugin
     * to ensure compatibility.
     *
     * @return A version string (e.g., "1.0").
     */
    String getVersion()

    /**
     * Analyzes the given build payload.
     *
     * This method will be called asynchronously by the plugin within a Gradle Worker.
     *
     * @param payload The sanitized build information, including logs and context.
     * @return A Response object containing the analysis from the LLM.
     */
    Response analyze(Payload payload)
}
