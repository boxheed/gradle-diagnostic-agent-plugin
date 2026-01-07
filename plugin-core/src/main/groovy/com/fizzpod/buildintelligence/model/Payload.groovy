package com.fizzpod.buildintelligence.model

import groovy.transform.Immutable

@Immutable
class Payload {
    String buildId
    String failureLog
    Map<String, Long> taskTimings
    Map<String, String> environmentContext
}
