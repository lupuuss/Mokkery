package dev.mokkery.internal.answering

import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass


internal fun <T> BlockingCallDefinitionScope(
    scope: FunctionScope
): BlockingCallDefinitionScope<T> = BlockingCallDefinitionScopeImpl(scope)

internal fun <T> SuspendCallDefinitionScope(
    scope: FunctionScope
): SuspendCallDefinitionScope<T> = SuspendCallDefinitionScopeImpl(scope)

private class BlockingCallDefinitionScopeImpl<R>(
    private val scope: FunctionScope
) : BlockingCallDefinitionScope<R> {

    override val returnType: KClass<*> get() = scope.returnType
    override val self: Any? get() = scope.self


    override fun callOriginal(): R = scope.callOriginal(scope.args).unsafeCast()

    override fun callOriginalWith(vararg args: Any?): R = scope.callOriginal(args.toList()).unsafeCast()

    override fun callSuper(type: KClass<*>): R  = scope.callSuper(type, scope.args).unsafeCast()

    override fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope.callSuper(type, args.toList()).unsafeCast()
}

private class SuspendCallDefinitionScopeImpl<R>(
    private val scope: FunctionScope
) : SuspendCallDefinitionScope<R> {

    override val returnType: KClass<*> get() = scope.returnType
    override val self: Any? get() = scope.self

    override suspend fun callOriginal(): R = scope.callSuspendOriginal(scope.args).unsafeCast()

    override suspend fun callOriginalWith(vararg args: Any?): R = scope.callSuspendOriginal(args.toList()).unsafeCast()

    override suspend fun callSuper(type: KClass<*>): R = scope.callSuspendSuper(type, scope.args).unsafeCast()

    override suspend fun callSuperWith(type: KClass<*>, vararg args: Any?): R = scope.callSuspendSuper(type, args.toList()).unsafeCast()
}
