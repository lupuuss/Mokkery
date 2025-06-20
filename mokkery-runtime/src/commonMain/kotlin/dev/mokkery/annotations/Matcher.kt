package dev.mokkery.annotations

/**
 * Marks composite matcher parameter that accepts other matchers.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class Matcher
