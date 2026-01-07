# Extending the BuildIntelligence Plugin

The BuildIntelligence plugin is designed to be extensible, allowing you to create and plug in your own analysis providers. This is useful for integrating with internal company AI services or using different LLM providers that are not included out-of-the-box.

## 1. The `LlmProvider` Interface

The core of the provider mechanism is the `com.example.buildintelligence.spi.LlmProvider` interface. Your custom provider must implement this interface.

```groovy
package com.example.buildintelligence.spi

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response

interface LlmProvider {
    String getVersion()
    Response analyze(Payload payload)
}
```

-   `getVersion()`: This should return the version of the SPI your provider is compatible with (e.g., `"1.0"`). The plugin will use this to ensure compatibility.
-   `analyze(Payload payload)`: This is the main method where you implement your analysis logic. It receives a `Payload` object containing the sanitized build data and must return a `Response` object.

## 2. Creating a Custom Provider

Here is an example of a custom provider:

```groovy
package com.mycompany.ai

import com.example.buildintelligence.model.Payload
import com.example.buildintelligence.model.Response
import com.example.buildintelligence.spi.LlmProvider

class MyInternalProvider implements LlmProvider {
    @Override
    String getVersion() {
        return "1.0"
    }

    @Override
    Response analyze(Payload payload) {
        // Your logic to call your internal AI service
        String result = callMyAiService(payload.failureLog)

        return new Response(
            analysis: result,
            providerId: "my-internal-provider",
            metadata: [:]
        )
    }

    private String callMyAiService(String log) {
        // ... implementation details ...
        return "Analysis from our internal service."
    }
}
```

## 3. Registering the Provider

To make your provider discoverable by the BuildIntelligence plugin, you must use Java's standard `ServiceLoader` mechanism.

1.  In your provider's source code, create the following directory: `src/main/resources/META-INF/services/`.
2.  Inside that directory, create a file named exactly `com.example.buildintelligence.spi.LlmProvider`.
3.  In this file, add a single line with the fully qualified class name of your provider implementation.

**File: `src/main/resources/META-INF/services/com.example.buildintelligence.spi.LlmProvider`**
```
com.mycompany.ai.MyInternalProvider
```

## 4. Using Your Custom Provider

1.  Package your provider implementation, including the `META-INF/services` file, into a JAR.
2.  Add this JAR to the `buildscript` classpath of the project where you are using the BuildIntelligence plugin.

**`build.gradle`**
```groovy
buildscript {
    repositories {
        mavenCentral() // or your internal repository
        flatDir {
            dirs 'libs' // if the JAR is in a local 'libs' folder
        }
    }
    dependencies {
        classpath 'com.mycompany.ai:my-provider:1.0.0'
    }
}

plugins {
    id 'com.example.build-intelligence'
}

buildIntelligence {
    // The provider name is derived from its simple class name, in lowercase
    activeProvider = "myinternalprovider"
}
```

The BuildIntelligence plugin will automatically discover and register your provider, making it available for configuration in the DSL.
