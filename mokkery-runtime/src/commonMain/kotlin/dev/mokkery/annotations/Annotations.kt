package dev.mokkery.annotations

/**
 * Marks parameter that accepts matchers.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
public annotation class Matcher

/**
 * Marks a top-level function that Mokkery calls when initializing [dev.mokkery.MokkeryScope.module]. Only initializers
 * compiled in the same module apply, e.g. initializers from main sources do not apply to tests.
 *
 * The function must not be private, suspend or generic. It may accept no parameters or a single
 * [dev.mokkery.configurer.MokkeryConfigurer] or [dev.mokkery.MokkeryScope], as a value parameter, extension receiver or
 * context parameter.
 *
 * Initializers must not access [dev.mokkery.MokkeryScope.module], directly or through Mokkery APIs called without an
 * explicit scope. Accept [dev.mokkery.MokkeryScope] instead.
 *
 * Initializers declared in the same file run in declaration order. The order across files is unspecified.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class MokkeryModuleInit
