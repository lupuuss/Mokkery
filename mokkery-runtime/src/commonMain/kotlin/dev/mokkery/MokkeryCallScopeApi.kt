package dev.mokkery

import dev.mokkery.answering.AnswerDeprecationMessage
import dev.mokkery.context.CallArgument
import dev.mokkery.context.FunctionCall
import dev.mokkery.context.require
import dev.mokkery.internal.IncorrectArgsForSpiedMethodException
import dev.mokkery.internal.IncorrectArgsForSuperMethodException
import dev.mokkery.internal.MissingSpyMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.assertSpy
import dev.mokkery.internal.context.associatedFunctions
import dev.mokkery.internal.context.mockSpec
import dev.mokkery.internal.utils.bestName
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

/**
 * Equivalent of `this` in the scope of currently called function.
 */
public val MokkeryCallScope.self: Any
    get() = mockSpec.thisRef

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
 * Calls original method implementation with given [args].
 */
public fun MokkeryBlockingCallScope.callOriginal(args: List<Any?>): Any? = callSuper(methodOriginType, args)

/**
 * Calls original method implementation with given [args].
 */
public suspend fun MokkerySuspendCallScope.callOriginal(args: List<Any?>): Any? = callSuper(methodOriginType, args)

/**
 * Calls super method of [superType] with given [args]
 */
public fun MokkeryBlockingCallScope.callSuper(superType: KClass<*>, args: List<Any?>): Any? {
    checkSuperArgs(args)
    return supers[superType]
        .let { it ?: throw MissingSuperMethodException(superType) }
        .unsafeCast<(List<Any?>) -> Any?>()
        .invoke(args)
}

/**
 * Calls super method of [superType] with given [args]
 */
public suspend fun MokkerySuspendCallScope.callSuper(superType: KClass<*>, args: List<Any?>): Any? {
    checkSuperArgs(args)
    return supers[superType]
        .let { it ?: throw MissingSuperMethodException(superType) }
        .unsafeCast<suspend (List<Any?>) -> Any?>()
        .invoke(args)
}

/**
 * Calls spied method with given [args].
 */
public fun MokkeryBlockingCallScope.callSpied(args: List<Any?>): Any? {
    assertSpy()
    checkSpiedArgs(args)
    return associatedFunctions
        .spiedFunction
        .let { it ?: throw MissingSpyMethodException() }
        .unsafeCast<(List<Any?>) -> Any?>()
        .invoke(args)
}

/**
 * Calls spied method with given [args].
 */
public suspend fun MokkerySuspendCallScope.callSpied(args: List<Any?>): Any? {
    assertSpy()
    checkSpiedArgs(args)
    return associatedFunctions
        .spiedFunction
        .let { it ?: throw MissingSpyMethodException() }
        .unsafeCast<suspend (List<Any?>) -> Any?>()
        .invoke(args)
}

/**
 * Creates [dev.mokkery.answering.FunctionScope] from this [MokkeryCallScope]
 */
@Deprecated(AnswerDeprecationMessage, level = DeprecationLevel.ERROR)
@Suppress("DEPRECATION_ERROR")
public fun MokkeryCallScope.toFunctionScope(): dev.mokkery.answering.FunctionScope {
    val call = call
    val function = call.function
    return dev.mokkery.answering.FunctionScope(
        returnType = function.returnType,
        args = call.args.map(CallArgument::value),
        self = self,
        supers = supers,
        classSupertypes = mockSpec.interceptedTypes.map { it.type }
    )
}

private val MokkeryCallScope.methodOriginType: KClass<*>
    get() {
        val supers = this.supers
        val interceptedTypes = mockSpec.interceptedTypes.map { it.type }
        val superCandidates = interceptedTypes.filter(supers::contains)
        if (superCandidates.isEmpty()) throw MissingSuperMethodException(interceptedTypes)
        val superType = superCandidates
            .singleOrNull()
            ?: throw SuperTypeMustBeSpecifiedException(
                "Multiple original super calls available ${superCandidates.map(KClass<*>::bestName)}!"
            )
        return superType
    }

private fun MokkeryCallScope.checkSuperArgs(args: List<Any?>) {
    if (call.args.size != args.size) {
        throw IncorrectArgsForSuperMethodException(call.args.size, args.size)
    }
}

private fun MokkeryCallScope.checkSpiedArgs(args: List<Any?>) {
    if (call.args.size != args.size) {
        throw IncorrectArgsForSpiedMethodException(call.args.size, args.size)
    }
}
