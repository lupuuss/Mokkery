@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package dev.mokkery.coroutines

import dev.mokkery.context.MokkeryContext
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.MokkerySuspendCallScope


suspend fun <T> Awaitable<T>.await(context: MokkeryContext = MokkeryContext.Empty): T {
    return await(createMokkerySuspendCallScope(context))
}

fun createMokkerySuspendCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuspendCallScope {
    return MokkerySuspendCallScope(context)
}

fun createMokkeryBlockingCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryBlockingCallScope {
    return MokkeryBlockingCallScope(context)
}
