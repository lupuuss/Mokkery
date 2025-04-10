package dev.mokkery.interceptor

import dev.mokkery.answering.FunctionScope
import dev.mokkery.context.CallArgument
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.mockContext
import dev.mokkery.internal.context.callInterceptor
import dev.mokkery.internal.interceptor.withContext
import dev.mokkery.internal.resolveInstance
import kotlin.reflect.KClass

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 */
public fun MokkeryBlockingCallScope.nextIntercept(): Any? = callInterceptor.intercept(this)

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 */
public suspend fun MokkerySuspendCallScope.nextIntercept(): Any? = callInterceptor.intercept(this)

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public fun MokkeryBlockingCallScope.nextIntercept(context: MokkeryContext): Any? = callInterceptor
    .intercept(withContext(context))

/**
 * Calls [MokkeryCallInterceptor.intercept] on the next interceptor in the pipeline.
 * Adds [context] to the next pipeline context.
 */
public suspend fun MokkerySuspendCallScope.nextIntercept(context: MokkeryContext): Any? = callInterceptor
    .intercept(withContext(context))

/**
 * Equivalent of `this` in the scope of currently called function.
 */
public val MokkeryCallScope.self: Any?
    get() = resolveInstance(mockContext.thisInstanceScope)

/**
 * Returns [MokkeryCallScope.self] as [T].
 */
public inline fun <reified T> MokkeryCallScope.self(): T = self as T

/**
 * Returns current call.
 */
public val MokkeryCallScope.call: FunctionCall
    get() = mokkeryContext.require(FunctionCall)

/**
 * Returns a map of available super calls for currently called function.
 */
public val MokkeryCallScope.supers: Map<KClass<*>, Function<Any?>>
    get() = associatedFunctions.supers

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
        supers = supers,
        classSupertypes = mockContext.interceptedTypes
    )
}
