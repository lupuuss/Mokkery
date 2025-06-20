package dev.mokkery.annotations

/**
 * Marks matcher that returns vararg matchers.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class VarArgMatcherBuilder
