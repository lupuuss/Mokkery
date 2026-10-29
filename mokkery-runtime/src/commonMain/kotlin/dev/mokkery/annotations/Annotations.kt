package dev.mokkery.annotations

/**
 * Marks parameter that accepts matchers.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.BINARY)
public annotation class Matcher

/**
 * Marks a top-level function that Mokkery calls when initializing [dev.mokkery.MokkeryScope.module], which happens
 * once, when Mokkery is first used in the module (e.g. a mock is created). Only initializers compiled in the same module
 * apply, e.g. initializers from main sources do not apply to tests.
 *
 * The function must not be private, suspend or generic. It may accept no parameters or a single
 * [dev.mokkery.configurer.MokkeryConfigurer] or [dev.mokkery.MokkeryScope], as a value parameter, extension receiver or
 * context parameter.
 *
 * The received scope is [dev.mokkery.MokkeryScope.module], so its configuration affects only mocks created in this
 * module. It is also possible to configure [dev.mokkery.MokkeryScope.global] to affect mocks created in other modules
 * (e.g. shared test utilities). However, such configuration applies only from the moment the initializer runs, so for
 * this to work correctly, the first Mokkery call must come from the module that declares the initializer.
 *
 * Initializers must not access [dev.mokkery.MokkeryScope.module], directly or through Mokkery APIs called without an
 * explicit scope. Accept [dev.mokkery.MokkeryScope] instead.
 *
 * Initializers declared in the same file run in declaration order. The order across files is unspecified.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class MokkeryModuleInit
