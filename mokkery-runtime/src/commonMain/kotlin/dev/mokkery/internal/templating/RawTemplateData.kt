package dev.mokkery.internal.templating

import dev.mokkery.matcher.ArgMatcher

internal class RawTemplateData(
    val matchers: MutableMap<String, List<ArgMatcher<Any?>>> = mutableMapOf(),
    var varargMatchersCount: Int = 0,
)
