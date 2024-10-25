package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.MokkeryContext.Empty
import dev.mokkery.interceptor.withContext
import dev.mokkery.internal.utils.mokkeryRuntimeError

internal fun combine(vararg interceptors: MokkeryCallInterceptor): MokkeryCallInterceptor {
    return RecursiveNextCallInterceptor(0, interceptors)
}

internal fun List<MokkeryCallInterceptor>.combined(): MokkeryCallInterceptor {
    return RecursiveNextCallInterceptor(0, this.toTypedArray())
}

internal inline val MokkeryContext.nextInterceptor: MokkeryCallInterceptor
    get(): MokkeryCallInterceptor {
        return get(NextCallInterceptor) ?: mokkeryRuntimeError("There is no next interceptor is the pipeline!")
    }

internal interface NextCallInterceptor : MokkeryCallInterceptor, MokkeryContext.Element {
    override val key get() = Key
    companion object Key : MokkeryContext.Key<NextCallInterceptor>
}

private class RecursiveNextCallInterceptor(
    private val index: Int,
    private val interceptors: Array<out MokkeryCallInterceptor>,
) : NextCallInterceptor {

    private val next = if (index + 1 < interceptors.size) {
        RecursiveNextCallInterceptor(index + 1, interceptors)
    } else {
        Empty
    }

    override fun intercept(scope: MokkeryCallScope): Any? {
        return interceptors[index].intercept(scope.withContext(next))
    }

    override suspend fun interceptSuspend(scope: MokkeryCallScope): Any? {
        return interceptors[index].interceptSuspend(scope.withContext(next))
    }
}
