@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

/**
 * Provides mock implementation of [T1] and [T2].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany2<T1, T2>.() -> Unit = { }
): MockMany2<T1, T2> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2] and [T3].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany3<T1, T2, T3>.() -> Unit = { }
): MockMany3<T1, T2, T3> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2], [T3] and [T4].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany4<T1, T2, T3, T4>.() -> Unit = { }
): MockMany4<T1, T2, T3, T4> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2], [T3], [T4] and [T5].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any, reified T5 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany5<T1, T2, T3, T4, T5>.() -> Unit = { }
): MockMany5<T1, T2, T3, T4, T5> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1] and [T2]. It is registered in this [MokkeryTestsScope].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any> MokkeryTestsScope.mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany2<T1, T2>.() -> Unit = { }
): MockMany2<T1, T2> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2] and [T3]. It is registered in this [MokkeryTestsScope].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any> MokkeryTestsScope.mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany3<T1, T2, T3>.() -> Unit = { }
): MockMany3<T1, T2, T3> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2], [T3] and [T4]. It is registered in this [MokkeryTestsScope].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> MokkeryTestsScope.mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany4<T1, T2, T3, T4>.() -> Unit = { }
): MockMany4<T1, T2, T3, T4> = throw MokkeryPluginNotAppliedException()

/**
 * Provides mock implementation of [T1], [T2], [T3], [T4] and [T5]. It is registered in this [MokkeryTestsScope].
 *
 * Types restrictions:
 * * Each type has to satisfy type restriction from [mock].
 * * Only one class is allowed
 * * No type duplicates
 */
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any, reified T5 : Any> MokkeryTestsScope.mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: MockMany5<T1, T2, T3, T4, T5>.() -> Unit = { }
): MockMany5<T1, T2, T3, T4, T5> = throw MokkeryPluginNotAppliedException()


/**
 * Marker interface for mock of [T1] and [T2].
 */
public interface MockMany2<T1 : Any, T2 : Any>

/**
 * Marker interface for mock of [T1], [T2] and [T3].
 */
public interface MockMany3<T1 : Any, T2 : Any, T3 : Any> : MockMany2<T1, T2>

/**
 * Marker interface for mock of [T1], [T2], [T3] and [T4].
 */
public interface MockMany4<T1 : Any, T2 : Any, T3 : Any, T4 : Any> : MockMany3<T1, T2, T3>

/**
 * Marker interface for mock of [T1], [T2], [T3], [T4] and [T5].
 */
public interface MockMany5<T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any> : MockMany4<T1, T2, T3, T4>

/**
 * Casts this as [T1].
 */
public inline val <reified T1 : Any> MockMany2<T1, *>.t1: T1 get() = this as T1

/**
 * Casts this as [T2].
 */
public inline val <reified T2 : Any> MockMany2<*, T2>.t2: T2 get() = this as T2

/**
 * Casts this as [T3].
 */
public inline val <reified T3 : Any> MockMany3<*, *, T3>.t3: T3 get() = this as T3

/**
 * Casts this as [T4].
 */
public inline val <reified T4 : Any> MockMany4<*, *, *, T4>.t4: T4 get() = this as T4

/**
 * Casts this as [T5].
 */
public inline val <reified T5 : Any> MockMany5<*, *, *, *, T5>.t5: T5 get() = this as T5
