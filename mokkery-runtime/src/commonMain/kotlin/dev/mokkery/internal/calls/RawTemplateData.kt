package dev.mokkery.internal.calls

import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal class RawTemplateData(
    val matchers: MutableMap<String, List<ArgMatcher<Any?>>> = mutableMapOf(),
    var varargMatchersCount: Int = 0,
    var genericReturnTypeHint: KClass<*>? = null
)
