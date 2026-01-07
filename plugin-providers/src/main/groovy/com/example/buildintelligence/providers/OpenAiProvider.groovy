package com.example.buildintelligence.providers

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response
import com.example.buildintelligence.spi.LlmProvider
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody

class OpenAiProvider implements LlmProvider {
    String apiKey
    String endpoint
    String model

    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    Response analyze(Payload payload) {
        def client = new OkHttpClient()

        def requestBody = JsonOutput.toJson([
                model: model,
                messages: [
                        [role: "system", content: "You are a build analysis expert. Analyze the following build failure and provide a root cause and a suggested fix."],
                        [role: "user", content: payload.failureLog]
                ]
        ])

        def request = new Request.Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $apiKey")
                .post(RequestBody.create(requestBody, MediaType.get("application/json")))
                .build()

        def httpResponse = client.newCall(request).execute()
        
        if (!httpResponse.isSuccessful()) {
            return new Response(
                analysis: "Failed to get analysis from OpenAI: ${httpResponse.code()} ${httpResponse.message()}",
                providerId: "openai-provider",
                metadata: [:]
            )
        }

        def responseBody = new JsonSlurper().parseText(httpResponse.body().string())
        def analysis = responseBody.choices[0].message.content

        return new Response(
            analysis: analysis,
            providerId: "openai-provider",
            metadata: [model: model]
        )
    }
}
