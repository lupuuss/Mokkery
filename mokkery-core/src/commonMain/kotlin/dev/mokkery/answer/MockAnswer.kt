package dev.mokkery.answer

import dev.mokkery.SuspendingFunctionBlockingCallException
import dev.mokkery.defaultValue
import kotlin.reflect.KClass

internal interface MockAnswer<out T> {

    fun call(returnType: KClass<*>, vararg args: Any?): T

    suspend fun callSuspend(returnType: KClass<*>, vararg args: Any?): T = call(returnType, *args)
}

internal class ConstAnswer<T>(private val value: T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, vararg args: Any?): T = value
}

internal class CustomAnswer<T>(private val block: (CallArgs) -> T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, vararg args: Any?): T = block(CallArgs(args.toList()))
}

internal class ThrowsAnswer(private val throwable: Throwable): MockAnswer<Nothing> {
    override fun call(returnType: KClass<*>, vararg args: Any?): Nothing = throw throwable
}

internal class CustomSuspendAnswer<T>(private val block: suspend (CallArgs) -> T) : MockAnswer<T> {
    override fun call(returnType: KClass<*>, vararg args: Any?): T = throw SuspendingFunctionBlockingCallException()

    override suspend fun callSuspend(returnType: KClass<*>, vararg args: Any?): T {
        return block(CallArgs(args.toList()))
    }
}

internal object DefaultAnswer : MockAnswer<Any?> {
    override fun call(returnType: KClass<*>, vararg args: Any?): Any? = defaultValue(returnType)
}
