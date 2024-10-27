package dev.mokkery.interceptor

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.FunctionScope
import dev.mokkery.context.CallArgument
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.call
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.self
import dev.mokkery.internal.interceptor.MokkeryCallHooks
import dev.mokkery.internal.interceptor.nextInterceptor
import kotlin.reflect.KClass

/**
 * It's invoked on each mocked function call.
 */
@DelicateMokkeryApi
public interface MokkeryCallInterceptor {

    public fun intercept(scope: MokkeryBlockingCallScope): Any?

    public suspend fun intercept(scope: MokkerySuspendCallScope): Any?

    public companion object {

        /**
         * Allows registering interceptors after a call is traced but before an answer is provided.
         */
        public val beforeAnswering: MokkeryHook<MokkeryCallInterceptor> = MokkeryCallHooks.beforeAnswering
    }
}

/**
 * Provides a set of operations available
 * in a [MokkeryCallInterceptor.intercept] and [MokkeryCallInterceptor.intercept].
 */
public interface MokkeryCallScope {

    public val context: MokkeryContext
}

/**
 * Provides a set of operations specific for [MokkeryCallInterceptor.intercept]
 */
public interface MokkeryBlockingCallScope : MokkeryCallScope

/**
 * Provides a set of operations specific for [MokkeryCallInterceptor.intercept]
 */
public interface MokkerySuspendCallScope : MokkeryCallScope

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
public fun MokkeryBlockingCallScope.nextIntercept(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.intercept(withContext(context))
}

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public suspend fun MokkerySuspendCallScope.nextIntercept(context: MokkeryContext = MokkeryContext.Empty): Any? {
    return this.context.nextInterceptor.intercept(withContext(context))
}

internal fun MokkeryBlockingCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkeryBlockingCallScope {

    return object : MokkeryBlockingCallScope {
        override val context = context

        override fun toString(): String = "MokkeryBlockingCallScope($context)"
    }
}

internal fun MokkerySuspendCallScope(context: MokkeryContext = MokkeryContext.Empty): MokkerySuspendCallScope {

    return object : MokkerySuspendCallScope {
        override val context = context

        override fun toString(): String = "MokkerySuspendCallScope($context)"
    }
}

internal fun MokkeryBlockingCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkeryBlockingCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkeryBlockingCallScope(this.context + with)
    }
}

internal fun MokkerySuspendCallScope.withContext(
    with: MokkeryContext = MokkeryContext.Empty
): MokkerySuspendCallScope {
    return when {
        with === MokkeryContext.Empty -> this
        else -> MokkerySuspendCallScope(this.context + with)
    }
}
