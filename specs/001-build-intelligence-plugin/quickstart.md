# Quickstart Guide: BuildIntelligence Gradle Plugin

This guide provides a brief overview of how to apply and configure the BuildIntelligence plugin in your Gradle project.

## 1. Apply the Plugin

First, apply the plugin to your `build.gradle` file. The plugin will be published with a specific ID.

**`build.gradle`**
```groovy
plugins {
    id 'com.example.build-intelligence' version '1.0.0'
}
```

## 2. Configure the Plugin

Next, configure the plugin using the `buildIntelligence` block. At a minimum, you need to define at least one provider and set it as the active provider.

**`build.gradle`**
```groovy
buildIntelligence {
    // Set the name of the provider you want to use
    activeProvider = "openai"

    // Define your provider configurations
    providers {
        openai {
            // It is STRONGLY recommended to load secrets from a secure source
            // such as gradle.properties, environment variables, or a secrets manager.
            apiKey = providers.gradleProperty("OPENAI_API_KEY")
            endpoint = "https://api.openai.com/v1/chat/completions"
        }
    }
}
```

**`gradle.properties`**
```properties
# Add your API key here. Do not commit this file to version control if it contains secrets.
OPENAI_API_KEY=your-api-key-here
```

## 3. How It Works

With this configuration:

1.  If your build fails, the plugin will automatically capture, truncate, and sanitize the failure data.
2.  The payload will be sent to the `openai` provider for analysis.
3.  The analysis will be printed to your console and saved to `build/reports/BuildAnalysis.md`.

## 4. Dry Run Mode

To see what data would be sent to the LLM without making a real API call, you can enable `dryRun` mode. This is useful for verifying your privacy and redaction settings.

**`build.gradle`**
```groovy
buildIntelligence {
    activeProvider = "openai"
    dryRun = true // Enable dry run mode
    // ... providers configuration
}
```

When `dryRun` is `true`, the plugin will log the final redacted and truncated payload to the console instead of sending it to the LLM.