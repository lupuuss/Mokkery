package dev.mokkery

internal open class MokkeryRuntimeException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

internal class CallNotMockedException(name: String) : MokkeryRuntimeException(message = "Call $name not mocked!")

@PublishedApi
internal class MokkeryPluginNotAppliedException : MokkeryRuntimeException(
    message = "This call should be replaced by the Mokkery plugin! Please make sure you applied the plugin correctly!"
)

internal class ObjectNotMockedMockedExcpetion(obj: Any?) : MokkeryRuntimeException("$obj is not a mock provided by Mokkery!")

internal class NotSingleCallInEveryBlockException : MokkeryRuntimeException("Each 'every' block requires single mock call!")

internal class MixingMatchersWithLiteralsException(
    signature: String
) : MokkeryRuntimeException("Mixing literals with matchers is illegal! Causing call: $signature")

internal class SuspendingFunctionBlockingCallException : MokkeryRuntimeException(
    message = "Regular function was mocked with suspending call!"
)
