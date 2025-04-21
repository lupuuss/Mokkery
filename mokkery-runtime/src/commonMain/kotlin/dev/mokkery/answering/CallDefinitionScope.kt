package dev.mokkery.answering

import dev.mokkery.context.argValues
import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.callOriginal
import dev.mokkery.callSuper
import dev.mokkery.self
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

/**
 * Provides a base set of operation for [BlockingAnsweringScope.calls] and [SuspendAnsweringScope.calls].
 */
public interface CallDefinitionScope<out R> {

    /**
     * Mocked method return type [KClass].
     */
    public val returnType: KClass<*>


    /**
     * It serves as a `this` and returns mock object.
     */
    public val self: Any?
}

/**
 * Returns [CallDefinitionScope.self] as [T].
 */
public inline fun <reified T> CallDefinitionScope<*>.self(): T = self as T

/**
 * Provides a set of operation for [BlockingAnsweringScope.calls].
 */
public interface BlockingCallDefinitionScope<out R> : CallDefinitionScope<R> {

    /**
     * Calls original implementation of mocked method with original args. For interfaces, it is default implementation.
     */
    public fun callOriginal(): R

    /**
     * Calls original implementation of mocked method with given [args]. For interfaces, it is default implementation.
     */
    public fun callOriginalWith(vararg args: Any?): R

    /**
     * Calls implementation of mocked method from super [type] with original args. For interfaces, it is default implementation of this [type].
     */
    public fun callSuper(type: KClass<*>): R

    /**
     * Calls implementation of mocked method from super [type] with given [args]. For interfaces, it is default implementation of this [type].
     */
    public fun callSuperWith(type: KClass<*>, vararg args: Any?): R
}

public fun <T> BlockingCallDefinitionScope(
    scope: MokkeryBlockingCallScope
): BlockingCallDefinitionScope<T> = BlockingCallDefinitionScopeImpl(scope)

@Deprecated(AnswerDeprecationMessage)
@Suppress("DEPRECATION")
public fun <T> BlockingCallDefinitionScope(
    scope: FunctionScope
): BlockingCallDefinitionScope<T> = DeprecatedBlockingCallDefinitionScopeImpl(scope)

/**
 * Provides a set of operation for [SuspendAnsweringScope.calls].
 */
public interface SuspendCallDefinitionScope<out R> : CallDefinitionScope<R> {


    /**
     * Calls original implementation of mocked method with original args. For interfaces, it is default implementation.
     */
    public suspend fun callOriginal(): R

    /**
     * Calls original implementation of mocked method with given [args]. For interfaces, it is default implementation.
     */
    public suspend fun callOriginalWith(vararg args: Any?): R

    /**
     * Calls implementation of mocked method from super [type] with original args. For interfaces, it is default implementation of this [type].
     */
    public suspend fun callSuper(type: KClass<*>): R

    /**
     * Calls implementation of mocked method from super [type] with given [args]. For interfaces, it is default implementation of this [type].
     */
    public suspend fun callSuperWith(type: KClass<*>, vararg args: Any?): R
}

public fun <T> SuspendCallDefinitionScope(
    scope: MokkerySuspendCallScope
): SuspendCallDefinitionScope<T> = SuspendCallDefinitionScopeImpl(scope)

@Deprecated(AnswerDeprecationMessage)
@Suppress("DEPRECATION")
public fun <T> SuspendCallDefinitionScope(
    scope: FunctionScope
): SuspendCallDefinitionScope<T> = DeprecatedSuspendCallDefinitionScopeImpl(scope)

private class BlockingCallDefinitionScopeImpl<R>(
    private val scope: MokkeryBlockingCallScope
) : BlockingCallDefinitionScope<R> {

    override val returnType: KClass<*>
        get() = scope.call.function.returnType
    override val self: Any?
        get() = scope.self

    override fun callOriginal(): R = scope.callOriginal(scope.call.argValues).unsafeCast()

    override fun callOriginalWith(vararg args: Any?): R = scope.callOriginal(args.toList()).unsafeCast()

    override fun callSuper(type: KClass<*>): R  = scope.callSuper(type, scope.call.argValues).unsafeCast()

    override fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope.callSuper(type, args.toList()).unsafeCast()
}

private class SuspendCallDefinitionScopeImpl<R>(
    private val scope: MokkerySuspendCallScope
) : SuspendCallDefinitionScope<R> {

    override val returnType: KClass<*>
        get() = scope.call.function.returnType
    override val self: Any?
        get() = scope.self

    override suspend fun callOriginal(): R = scope.callOriginal(scope.call.argValues).unsafeCast()

    override suspend fun callOriginalWith(vararg args: Any?): R = scope.callOriginal(args.toList()).unsafeCast()

    override suspend fun callSuper(type: KClass<*>): R = scope.callSuper(type, scope.call.argValues).unsafeCast()

    override suspend fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope
        .callSuper(type, args.toList())
        .unsafeCast()
}


@Suppress("DEPRECATION")
private class DeprecatedBlockingCallDefinitionScopeImpl<R>(
    private val scope: FunctionScope
) : BlockingCallDefinitionScope<R> {

    override val returnType: KClass<*>
        get() = scope.returnType
    override val self: Any?
        get() = scope.self

    override fun callOriginal(): R = scope.callOriginal(scope.args).unsafeCast()

    override fun callOriginalWith(vararg args: Any?): R = scope.callOriginal(args.toList()).unsafeCast()

    override fun callSuper(type: KClass<*>): R  = scope.callSuper(type, scope.args).unsafeCast()

    override fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope.callSuper(type, args.toList()).unsafeCast()
}

@Suppress("DEPRECATION")
private class DeprecatedSuspendCallDefinitionScopeImpl<R>(
    private val scope: FunctionScope
) : SuspendCallDefinitionScope<R> {

    override val returnType: KClass<*>
        get() = scope.returnType
    override val self: Any?
        get() = scope.self

    override suspend fun callOriginal(): R = scope.callSuspendOriginal(scope.args).unsafeCast()

    override suspend fun callOriginalWith(vararg args: Any?): R = scope.callSuspendOriginal(args.toList()).unsafeCast()

    override suspend fun callSuper(type: KClass<*>): R = scope.callSuspendSuper(type, scope.args).unsafeCast()

    override suspend fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope.callSuspendSuper(type, args.toList()).unsafeCast()
}
