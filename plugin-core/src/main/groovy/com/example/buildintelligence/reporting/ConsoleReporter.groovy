package com.example.buildintelligence.reporting

import com.example.buildintelligence.model.Response

class ConsoleReporter {
    void report(Response response) {
        println "=== Build Intelligence Analysis ==="
        println "Provider: ${response.providerId}"
        println "Analysis: ${response.analysis}"
        println "==================================="
    }
}
