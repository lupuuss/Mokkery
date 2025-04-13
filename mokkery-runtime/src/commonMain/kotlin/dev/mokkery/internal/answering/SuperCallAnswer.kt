package dev.mokkery.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.context.argValues
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.callOriginal
import dev.mokkery.interceptor.callSuper
import dev.mokkery.internal.utils.description
import dev.mokkery.internal.utils.unsafeCast

@Poko
internal class SuperCallAnswer<T>(
    private val superCall: SuperCall,
) : Answer<T> {

    override fun call(scope: MokkeryBlockingCallScope): T = when (superCall) {
        is SuperCall.OfType -> scope.callSuper(superCall.type, superCall.args ?: scope.call.argValues)
        is SuperCall.Original -> scope.callOriginal(superCall.args ?: scope.call.argValues)
    }.unsafeCast()

    override suspend fun call(scope: MokkerySuspendCallScope): T = when (superCall) {
        is SuperCall.OfType -> scope.callSuper(superCall.type, superCall.args ?: scope.call.argValues)
        is SuperCall.Original -> scope.callOriginal(superCall.args ?: scope.call.argValues)
    }.unsafeCast()

    override fun description(): String  {
        val callDescription = when (superCall) {
            is SuperCall.OfType -> when (superCall.args) {
                null -> "superOf<${superCall.type.simpleName}>()"
                else -> "superWith<${superCall.type.simpleName}>(${superCall.args.joinToString { it.description() }})"
            }
            is SuperCall.Original -> when (superCall.args) {
                null -> "original"
                else -> "originalWith(${superCall.args.joinToString { it.description() }})"
            }
        }
        return "calls $callDescription"
    }
}
