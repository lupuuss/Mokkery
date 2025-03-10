package dev.mokkery.interceptor

import dev.mokkery.internal.MissingArgsForSuperMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.context.mockContext
import dev.mokkery.internal.utils.bestName
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

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
    checkArgs(args)
    return supers[superType]
        .let { it ?: throw MissingSuperMethodException(superType) }
        .unsafeCast<(List<Any?>) -> Any?>()
        .invoke(args)
}

/**
 * Calls super method of [superType] with given [args]
 */
public suspend fun MokkerySuspendCallScope.callSuper(superType: KClass<*>, args: List<Any?>): Any? {
    checkArgs(args)
    return supers[superType]
        .let { it ?: throw MissingSuperMethodException(superType) }
        .unsafeCast<suspend (List<Any?>) -> Any?>()
        .invoke(args)
}

private val MokkeryCallScope.methodOriginType: KClass<*>
    get() {
        val supers = this.supers
        val interceptedTypes = mockContext.interceptedTypes
        val superCandidates = interceptedTypes.filter(supers::contains)
        if (superCandidates.isEmpty()) throw MissingSuperMethodException(interceptedTypes)
        val superType = superCandidates
            .singleOrNull()
            ?: throw SuperTypeMustBeSpecifiedException(
                "Multiple original super calls available ${superCandidates.map(KClass<*>::bestName)}!"
            )
        return superType
    }

private fun MokkeryCallScope.checkArgs(args: List<Any?>) {
    if (call.args.size != args.size) {
        throw MissingArgsForSuperMethodException(call.args.size, args.size)
    }
}
