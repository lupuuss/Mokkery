package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuperCall
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

internal class SuperCallAnswer<T>(
    private val call: SuperCall,
    private val lookup: MokkeryScopeLookup = MokkeryScopeLookup.current,
) : Answer<T> {

    @Suppress("UNCHECKED_CAST")
    override fun call(scope: FunctionScope): T = when (call) {
        is SuperCall.OfType -> scope.callSuper(call.type, call.args ?: scope.args)
        is SuperCall.Original -> scope.callOriginal(lookup, call.args ?: scope.args)
    } as T
}
