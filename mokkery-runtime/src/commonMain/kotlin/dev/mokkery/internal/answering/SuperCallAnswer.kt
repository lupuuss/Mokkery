package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuperCall
import dev.mokkery.internal.description
import dev.mokkery.internal.dynamic.MokkeryInstanceLookup
import dev.mokkery.internal.unsafeCast


internal class SuperCallAnswer<T>(
    private val call: SuperCall,
    private val lookup: MokkeryInstanceLookup = MokkeryInstanceLookup.current,
) : Answer<T> {

    override fun call(scope: FunctionScope): T = when (call) {
        is SuperCall.OfType -> scope.callSuper(call.type, call.args ?: scope.args)
        is SuperCall.Original -> scope.callOriginal(lookup, call.args ?: scope.args)
    }.unsafeCast()

    override suspend fun callSuspend(scope: FunctionScope): T = when (call) {
        is SuperCall.OfType -> scope.callSuspendSuper(call.type, call.args ?: scope.args)
        is SuperCall.Original -> scope.callSuspendOriginal(lookup, call.args ?: scope.args)
    }.unsafeCast()

    override fun description(): String  {
        val callDescription = when (call) {
            is SuperCall.OfType -> when (call.args) {
                null -> "superOf<${call.type.simpleName}>()"
                else -> "superWith<${call.type.simpleName}>(${call.args.joinToString { it.description() }})"
            }
            is SuperCall.Original -> when (call.args) {
                null -> "original"
                else -> "originalWith(${call.args.joinToString { it.description() }})"
            }
        }
        return "calls $callDescription"
    }
}
