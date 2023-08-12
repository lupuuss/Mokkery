package dev.mokkery.answering

import kotlin.reflect.KClass

/**
 * Provides a base set of operation for [BlockingAnsweringScope.calls] and [SuspendAnsweringScope.calls].
 */
public interface CallDefinitionScope<out R> {

    /**
     * Mocked method return type [KClass].
     */
    public val returnType: KClass<*>


    /**
     * It serves as a `this` and returns mock object.
     */
    public val self: Any?
}

/**
 * Returns [CallDefinitionScope.self] as [R].
 */
public inline fun <reified R> CallDefinitionScope<R>.self(): R = self as R

/**
 * Provides a set of operation for [BlockingAnsweringScope.calls].
 */
public interface BlockingCallDefinitionScope<out R> : CallDefinitionScope<R> {

    /**
     * Calls original implementation of mocked method with original args. For interfaces, it is default implementation.
     */
    public fun callOriginal(): R

    /**
     * Calls original implementation of mocked method with given [args]. For interfaces, it is default implementation.
     */
    public fun callOriginalWith(vararg args: Any?): R

    /**
     * Calls implementation of mocked method from super [type] with original args. For interfaces, it is default implementation of this [type].
     */
    public fun callSuper(type: KClass<*>): R

    /**
     * Calls implementation of mocked method from super [type] with given [args]. For interfaces, it is default implementation of this [type].
     */
    public fun callSuperWith(type: KClass<*>, vararg args: Any?): R
}


/**
 * Provides a set of operation for [SuspendAnsweringScope.calls].
 */
public interface SuspendCallDefinitionScope<out R> : CallDefinitionScope<R> {


    /**
     * Calls original implementation of mocked method with original args. For interfaces, it is default implementation.
     */
    public suspend fun callOriginal(): R

    /**
     * Calls original implementation of mocked method with given [args]. For interfaces, it is default implementation.
     */
    public suspend fun callOriginalWith(vararg args: Any?): R

    /**
     * Calls implementation of mocked method from super [type] with original args. For interfaces, it is default implementation of this [type].
     */
    public suspend fun callSuper(type: KClass<*>): R

    /**
     * Calls implementation of mocked method from super [type] with given [args]. For interfaces, it is default implementation of this [type].
     */
    public suspend fun callSuperWith(type: KClass<*>, vararg args: Any?): R
}
