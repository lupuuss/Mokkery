package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.context.Function
import dev.mokkery.context.argValues
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.rendering.descriptor.FunctionRenderDescriptor

internal class DefaultsExtractingInterceptor(
    private val functionId: Function.Id,
) : ContextCallInterceptor {

    override fun intercept(scope: MokkeryBlockingCallScope): Nothing = scope.throwArguments()

    override suspend fun intercept(scope: MokkerySuspendCallScope): Nothing = scope.throwArguments()

    private fun MokkeryCallScope.throwArguments(): Nothing {
        val call = call
        if (call.function.id != functionId) {
            throw UnsupportedDefaultValueException(call.function.name.readableName())
        }
        throw ArgumentsExtractedException(call.argValues)
    }
}

private fun String.readableName(): String = FunctionRenderDescriptor.parse(this).name

internal class UnsupportedDefaultValueException(
    val usedMember: String
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")

internal class ArgumentsExtractedException(
    val values: List<Any?>
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")
