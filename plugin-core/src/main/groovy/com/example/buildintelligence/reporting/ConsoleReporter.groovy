package com.example.buildintelligence.reporting

import com.example.buildintelligence.model.Response
import org.gradle.api.logging.Logging

class ConsoleReporter {
    void report(Response response) {
        def logger = Logging.getLogger(ConsoleReporter.class)
        logger.lifecycle "=== Build Intelligence Analysis ==="
        logger.lifecycle "Provider: ${response.providerId}"
        logger.lifecycle "Analysis: ${response.analysis}"
        logger.lifecycle "==================================="
    }
}
