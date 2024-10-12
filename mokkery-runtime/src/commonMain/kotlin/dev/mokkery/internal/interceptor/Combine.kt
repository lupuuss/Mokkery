package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.MokkeryContext.Empty
import dev.mokkery.interceptor.withContext
import dev.mokkery.internal.mokkeryRuntimeError

internal fun combine(vararg interceptors: MokkeryCallInterceptor): MokkeryCallInterceptor {
    return NextCallInterceptor(0, interceptors)
}

internal inline val MokkeryContext.nextInterceptor: MokkeryCallInterceptor
    get(): MokkeryCallInterceptor {
        return get(NextCallInterceptor) ?: mokkeryRuntimeError("There is no next interceptor is the pipeline!")
    }

internal class NextCallInterceptor(
    private val index: Int,
    private val interceptors: Array<out MokkeryCallInterceptor>,
) : MokkeryCallInterceptor, MokkeryContext.Element {

    override val key = Key

    private val next = if (index + 1 < interceptors.size) {
        NextCallInterceptor(index + 1, interceptors)
    } else {
        Empty
    }

    override fun intercept(scope: MokkeryCallScope): Any? {
        return interceptors[index].intercept(scope.withContext(next))
    }

    override suspend fun interceptSuspend(scope: MokkeryCallScope): Any? {
        return interceptors[index].interceptSuspend(scope.withContext(next))
    }

    companion object Key : MokkeryContext.Key<NextCallInterceptor>
}
