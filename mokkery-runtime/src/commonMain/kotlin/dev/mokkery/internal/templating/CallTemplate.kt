package dev.mokkery.internal.templating

import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val instanceId: MokkeryInstanceId,
    val name: String,
    val parameters: List<Function.Parameter>,
    val matchers: Map<String, ArgMatcher<Any?>>
)
