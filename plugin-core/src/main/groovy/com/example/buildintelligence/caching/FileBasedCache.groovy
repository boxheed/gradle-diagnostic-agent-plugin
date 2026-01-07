package com.example.buildintelligence.caching

import com.example.buildintelligence.model.Response

import java.security.MessageDigest

class FileBasedCache {
    private final File cacheDir

    FileBasedCache(File rootDir) {
        this.cacheDir = new File(rootDir, "build-intelligence-cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    Response get(String key) {
        def cacheFile = new File(cacheDir, hashKey(key))
        if (cacheFile.exists()) {
            // In a real implementation, we would deserialize a Response object
            // For now, we'll just return a mock response indicating a cache hit.
            return new Response(
                analysis: "This is a cached analysis.",
                providerId: "cache",
                metadata: [:]
            )
        }
        return null
    }

    void put(String key, Response response) {
        def cacheFile = new File(cacheDir, hashKey(key))
        // In a real implementation, we would serialize the Response object
        cacheFile.text = response.analysis
    }

    private String hashKey(String key) {
        MessageDigest.getInstance("SHA-256").digest(key.bytes).encodeHex().toString()
    }
}
