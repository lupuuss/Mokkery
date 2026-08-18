package dev.mokkery.internal.defaults

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.call
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.context.ContextCallInterceptor

internal object DefaultsExtractingInterceptor : ContextCallInterceptor {

    override fun intercept(scope: MokkeryBlockingCallScope): Nothing = scope.throwArguments()

    override suspend fun intercept(scope: MokkerySuspendCallScope): Nothing = scope.throwArguments()

    private fun MokkeryCallScope.throwArguments(): Nothing {
        throw ArgumentsExtractedException(call.args.map(CallArgument::value))
    }
}

internal class ArgumentsExtractedException(
    val values: List<Any?>
) : MokkeryRuntimeException("This exception should be caught by the internal machinery!")
