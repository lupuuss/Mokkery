package dev.mokkery

/**
 * Base class for all Mokkery runtime exceptions.
 */
public open class MokkeryRuntimeException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)
