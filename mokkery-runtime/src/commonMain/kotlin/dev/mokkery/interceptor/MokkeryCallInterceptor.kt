package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.FunctionScope
import dev.mokkery.context.CallArgument
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.call
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.self
import dev.mokkery.internal.interceptor.nextInterceptor
import kotlin.reflect.KClass

/**
 * It's invoked on each mocked function call.
 */
@DelicateMokkeryApi
public interface MokkeryCallInterceptor {

    public fun intercept(scope: MokkeryCallScope): Any?

    public suspend fun interceptSuspend(scope: MokkeryCallScope): Any?
}

/**
 * Provides a set of operations available
 * in a [MokkeryCallInterceptor.intercept] and [MokkeryCallInterceptor.interceptSuspend].
 */
public interface MokkeryCallScope {

    public val context: MokkeryContext
}

/**
 * Equivalent of `this` in the scope of currently called function.
 */
public val MokkeryCallScope.self: Any?
    get() = context.self

/**
 * Returns current call.
 */
public val MokkeryCallScope.call: FunctionCall
    get() = context.call

/**
 * Returns a map of available super calls for currently called function.
 */
public val MokkeryCallScope.supers: Map<KClass<*>, Function<Any?>>
    get() = context.associatedFunctions.supers

/**
 * Creates [FunctionScope] from this [MokkeryCallScope]
 */
public fun MokkeryCallScope.toFunctionScope(): FunctionScope {
    val call = call
    val function = call.function
    return FunctionScope(
        returnType = function.returnType,
        args = call.args.map(CallArgument::value),
        self = self,
        supers = supers
    )
}

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public fun MokkeryCallScope.nextIntercept(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.intercept(withContext(context))
}

/**
 * Calls [MokkeryCallInterceptor.interceptSuspend] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public suspend fun MokkeryCallScope.nextInterceptSuspend(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.interceptSuspend(withContext(context))
}

internal fun MokkeryCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryCallScope {
    return object : MokkeryCallScope {
        override val context = context

        override fun toString(): String = "MokkeryCallScope($context)"
    }
}

internal fun MokkeryCallScope.withContext(with: MokkeryContext = MokkeryContext.Empty): MokkeryCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkeryCallScope(this.context + with)
    }
}
