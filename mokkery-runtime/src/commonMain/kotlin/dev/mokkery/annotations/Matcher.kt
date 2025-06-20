package dev.mokkery.annotations

/**
 * Marks composite matcher parameter that accepts other matchers.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
public annotation class Matcher
