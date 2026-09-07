package dev.mokkery.internal.templating

import dev.mokkery.context.Function
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val instanceId: MokkeryInstanceId,
    val functionId: Function.Id,
    val matchers: List<ArgMatcher<Any?>>
)
