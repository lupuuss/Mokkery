package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.MokkeryContext.Empty
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkerySuspendCallScope

internal inline val MokkeryScope.mockInterceptor: MokkeryCallInterceptor
    get() = mokkeryContext.require(MockInterceptor)

internal interface MockInterceptor : MokkeryCallInterceptor, MokkeryContext.Element {

    override val key get() = Key

    companion object Key : MokkeryContext.Key<MockInterceptor>
}

internal fun MockInterceptor(interceptors: List<MokkeryCallInterceptor>): MockInterceptor {
    return RecursiveMockInterceptor(0, interceptors.toTypedArray())
}
internal fun MockInterceptor(vararg interceptors: MokkeryCallInterceptor): MockInterceptor {
    return RecursiveMockInterceptor(0, interceptors)
}

private class RecursiveMockInterceptor(
    private val index: Int,
    private val interceptors: Array<out MokkeryCallInterceptor>,
) : MockInterceptor {

    private val next = if (index + 1 < interceptors.size) {
        RecursiveMockInterceptor(index + 1, interceptors)
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
