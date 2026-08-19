package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.internal.context.ContextCallInterceptor
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.rendering.descriptor.FunctionRenderDescriptor

internal class DefaultsExtractingInterceptor(
    private val functionName: String,
    private val parameters: List<Function.Parameter>,
) : ContextCallInterceptor {

    override fun intercept(scope: MokkeryBlockingCallScope): Nothing = scope.throwArguments()

    override suspend fun intercept(scope: MokkerySuspendCallScope): Nothing = scope.throwArguments()

    private fun MokkeryCallScope.throwArguments(): Nothing {
        val call = call
        if (!call.isExtractedFunction()) throw UnsupportedDefaultValueException(call.function.name.readableName())
        throw ArgumentsExtractedException(call.args.map(CallArgument::value))
    }

    private fun FunctionCall.isExtractedFunction(): Boolean {
        if (function.name != functionName) return false
        if (args.size != parameters.size) return false
        for (index in args.indices) if (args[index].parameter != parameters[index]) return false
        return true
    }
}

private fun String.readableName(): String = FunctionRenderDescriptor.parse(this).name

internal class UnsupportedDefaultValueException(
    val usedMember: String
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")

internal class ArgumentsExtractedException(
    val values: List<Any?>
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")
