package dev.mokkery

public open class MokkeryRuntimeException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
