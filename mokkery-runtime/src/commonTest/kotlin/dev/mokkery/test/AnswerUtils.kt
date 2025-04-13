package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.interceptor.MokkeryBlockingCallScope
import dev.mokkery.internal.interceptor.MokkerySuspendCallScope

fun <T> Answer<T>.callBlocking(
    context: MokkeryContext = MokkeryContext.Empty
): T = call(MokkeryBlockingCallScope(context))

suspend fun <T> Answer<T>.callSuspend(
    context: MokkeryContext = MokkeryContext.Empty
): T = call(MokkerySuspendCallScope(context))
