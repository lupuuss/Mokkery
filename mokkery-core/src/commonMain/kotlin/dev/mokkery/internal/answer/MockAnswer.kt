package dev.mokkery.internal.answer

import dev.mokkery.answer.CallArgs
import dev.mokkery.internal.SuspendingFunctionBlockingCallException
import kotlin.reflect.KClass

internal interface MockAnswer<out T> {

    fun call(returnType: KClass<*>, args: List<Any?>): T

    suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T = call(returnType, args)
}

internal class ConstAnswer<T>(private val value: T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, args: List<Any?>): T = value
}

internal class CustomAnswer<T>(private val block: (CallArgs) -> T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, args: List<Any?>): T = block(CallArgs(args))
}

internal class ThrowsAnswer(private val throwable: Throwable) : MockAnswer<Nothing> {
    override fun call(returnType: KClass<*>, args: List<Any?>): Nothing = throw throwable
}

internal class CustomSuspendAnswer<T>(private val block: suspend (CallArgs) -> T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, args: List<Any?>): T = throw SuspendingFunctionBlockingCallException()

    override suspend fun callSuspend(returnType: KClass<*>, args: List<Any?>): T {
        return block(CallArgs(args))
    }
}

internal object DefaultAnswer : MockAnswer<Any?> {
    override fun call(returnType: KClass<*>, args: List<Any?>): Any? = defaultValue(returnType)
}
