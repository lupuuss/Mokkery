package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope

fun <T> Answer<T>.callBlocking(
    context: MokkeryContext = MokkeryContext.Empty
): T = call(MokkeryBlockingCallScope(context))

suspend fun <T> Answer<T>.callSuspend(
    context: MokkeryContext = MokkeryContext.Empty
): T = call(MokkerySuspendCallScope(context))
