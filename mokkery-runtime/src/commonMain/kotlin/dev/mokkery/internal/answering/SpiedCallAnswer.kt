package dev.mokkery.internal.answering

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.answering.Answer
import dev.mokkery.call
import dev.mokkery.callSpied
import dev.mokkery.context.argValues

internal object SpiedCallAnswer : Answer<Any?> {

    override fun call(scope: MokkeryBlockingCallScope): Any? = scope.callSpied(scope.call.argValues)

    override suspend fun call(scope: MokkerySuspendCallScope): Any? = scope.callSpied(scope.call.argValues)
}
