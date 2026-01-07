package com.example.buildintelligence.services

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class MetricsService implements BuildService<BuildServiceParameters> {
    // This service would hold a metrics registry (e.g., Micrometer)
    // and be used by other services to record metrics like:
    // - cache hits/misses
    // - provider API call latency
    // - analysis success/failure counts

    void recordCacheHit() {
        // e.g., registry.counter("buildintelligence.cache.hits").increment()
    }

    void recordCacheMiss() {
        // e.g., registry.counter("buildintelligence.cache.misses").increment()
    }
}
