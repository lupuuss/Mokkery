package dev.mokkery

import dev.mokkery.context.FunctionCall
import dev.mokkery.context.require
import dev.mokkery.internal.IncorrectArgsForSpiedMethodException
import dev.mokkery.internal.IncorrectArgsForSuperMethodException
import dev.mokkery.internal.MissingSpyMethodException
import dev.mokkery.internal.MissingSuperMethodException
import dev.mokkery.internal.SuperTypeMustBeSpecifiedException
import dev.mokkery.internal.availableSuperCallTypes
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.requireSpy
import dev.mokkery.internal.contracts.SpyCallsContract
import dev.mokkery.internal.contracts.SuperCallsContract
import dev.mokkery.internal.contracts.spyCallsContract
import dev.mokkery.internal.contracts.superCallsContract
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

/**
 * Equivalent of `this` in the scope of currently called function.
 */
public val MokkeryCallScope.self: Any
    get() = instanceSpec.thisRef

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
    get() {
        val types = availableSuperCallTypes()
        if (types.isEmpty()) return emptyMap()
        return when (this) {
            is MokkerySuspendCallScope -> types.associateWith { this.superCallLambda(it) }
            is MokkeryBlockingCallScope -> types.associateWith { this.superCallLambda(it) }
            else -> mokkeryRuntimeError("Unknown call scope implementation ${this::class.simpleName}!")
        }
    }

private fun MokkerySuspendCallScope.superCallLambda(type: KClass<*>) = suspend { it: List<Any?> ->
    this.callSuper(type, it)
}

private fun MokkeryBlockingCallScope.superCallLambda(type: KClass<*>) = { it: List<Any?> ->
    this.callSuper(type, it)
}


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
public fun MokkeryBlockingCallScope.callSuper(superType: KClass<*>, args: List<Any?>): Any? =
    dispatchSuper(superType, args) { dispatcher, memberId, superTypeIndex ->
        dispatcher.mokkerySuperCall(memberId, superTypeIndex, args)
    }

/**
 * Calls super method of [superType] with given [args]
 */
public suspend fun MokkerySuspendCallScope.callSuper(superType: KClass<*>, args: List<Any?>): Any? =
    dispatchSuper(superType, args) { dispatcher, memberId, superTypeIndex ->
        dispatcher.mokkerySuperCallSuspend(memberId, superTypeIndex, args)
    }

private inline fun <R> MokkeryCallScope.dispatchSuper(
    superType: KClass<*>,
    args: List<Any?>,
    dispatch: (SuperCallsContract, memberId: Int, superTypeIndex: Int) -> R,
): R {
    checkSuperArgs(args)
    val contract = superCallsContract ?: throw MissingSuperMethodException(superType)
    val memberId = call.function.id
    val superTypeIndex = contract.mokkerySuperTypes(memberId).indexOf(superType)
    if (superTypeIndex < 0) throw MissingSuperMethodException(superType)
    return dispatch(contract, memberId, superTypeIndex)
}

/**
 * Calls spied method with given [args].
 */
public fun MokkeryBlockingCallScope.callSpied(args: List<Any?>): Any? = requireSpyContract(args)
    .mokkerySpyCall(call.function.id, args)

/**
 * Calls spied method with given [args].
 */
public suspend fun MokkerySuspendCallScope.callSpied(args: List<Any?>): Any? = requireSpyContract(args)
    .mokkerySpyCallSuspend(call.function.id, args)

private fun MokkeryCallScope.requireSpyContract(args: List<Any?>): SpyCallsContract {
    instanceSpec.requireSpy()
    checkSpiedArgs(args)
    return spyCallsContract ?: throw MissingSpyMethodException()
}

private val MokkeryCallScope.methodOriginType: KClass<*>
    get() {
        val supers = availableSuperCallTypes()
        val interceptedTypes = instanceSpec.interceptedTypes.map { it.type }
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
