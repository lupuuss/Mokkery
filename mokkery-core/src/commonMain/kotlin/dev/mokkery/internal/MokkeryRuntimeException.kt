package dev.mokkery.internal

internal open class MokkeryRuntimeException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

internal class CallNotMockedException(name: String) : MokkeryRuntimeException(message = "Call $name not mocked!")

@PublishedApi
internal class MokkeryPluginNotAppliedException : MokkeryRuntimeException(
    message = "This call should be replaced by the Mokkery plugin! Please make sure you applied the plugin correctly!"
)

internal class ObjectNotSpiedException(obj: Any?) : MokkeryRuntimeException("$obj is not spied by Mokkery!")

internal class ObjectNotMockedException(obj: Any?) : MokkeryRuntimeException("$obj is not mocked by Mokkery!")

internal class NotSingleCallInEveryBlockException : MokkeryRuntimeException("Each 'every' block requires single mock call!")

internal class MixingMatchersWithLiteralsException(
    signature: String
) : MokkeryRuntimeException("Mixing literals with matchers is illegal! Causing call: $signature")

internal class SuspendingFunctionBlockingCallException : MokkeryRuntimeException(
    message = "Regular function was mocked with suspending call!"
)

internal class ConcurrentTemplatingException : MokkeryRuntimeException(
    "Any concurrent calls involving verify and every are illegal!"
)
