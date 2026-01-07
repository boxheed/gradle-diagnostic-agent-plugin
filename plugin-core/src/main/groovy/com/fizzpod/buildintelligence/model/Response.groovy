package com.fizzpod.buildintelligence.model

import groovy.transform.Immutable

@Immutable
class Response {
    String analysis
    String providerId
    Map<String, String> metadata
}
