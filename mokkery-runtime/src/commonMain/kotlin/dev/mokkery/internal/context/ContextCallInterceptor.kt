package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.MokkeryContext.Empty
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.internal.interceptor.withContext

internal inline val MokkeryScope.callInterceptor: MokkeryCallInterceptor
    get() = mokkeryContext.require(ContextCallInterceptor)

internal interface ContextCallInterceptor : MokkeryCallInterceptor, MokkeryContext.Element {

    override val key get() = Key

    companion object Key : MokkeryContext.Key<ContextCallInterceptor>
}

internal fun ContextCallInterceptor(interceptors: List<MokkeryCallInterceptor>): ContextCallInterceptor {
    return RecursiveContextCallInterceptor(0, interceptors.toTypedArray())
}
internal fun ContextCallInterceptor(vararg interceptors: MokkeryCallInterceptor): ContextCallInterceptor {
    return RecursiveContextCallInterceptor(0, interceptors)
}

private class RecursiveContextCallInterceptor(
    private val index: Int,
    private val interceptors: Array<out MokkeryCallInterceptor>,
) : ContextCallInterceptor {

    private val next = if (index + 1 < interceptors.size) {
        RecursiveContextCallInterceptor(index + 1, interceptors)
    } else {
        Empty
    }

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        return interceptors[index].intercept(scope.withContext(next))
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        return interceptors[index].intercept(scope.withContext(next))
    }

    override fun toString(): String = "RecursiveMockInterceptor(index=$index)"
}
