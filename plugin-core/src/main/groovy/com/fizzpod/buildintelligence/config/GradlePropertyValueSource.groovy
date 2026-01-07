package com.fizzpod.buildintelligence.config

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.provider.Property

interface GradlePropertyParameters extends ValueSourceParameters {
    Property<String> getPropertyName()
}

abstract class GradlePropertyValueSource implements ValueSource<String, GradlePropertyParameters> {
    @Override
    String obtain() {
        return parameters.propertyName.map { System.getProperty(it) }.getOrNull()
    }
}
