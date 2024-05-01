package dev.mokkery

import dev.mokkery.internal.MokkeryPluginNotAppliedException

@Suppress("UNUSED_PARAMETER")
public inline fun <reified T1 : Any, reified T2 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: ManyMocks2<T1, T2>.() -> Unit = { }
): ManyMocks2<T1, T2> = throw MokkeryPluginNotAppliedException()

@Suppress("UNUSED_PARAMETER")
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: ManyMocks3<T1, T2, T3>.() -> Unit = { }
): ManyMocks3<T1, T2, T3> = throw MokkeryPluginNotAppliedException()

@Suppress("UNUSED_PARAMETER")
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: ManyMocks4<T1, T2, T3, T4>.() -> Unit = { }
): ManyMocks4<T1, T2, T3, T4> = throw MokkeryPluginNotAppliedException()

@Suppress("UNUSED_PARAMETER")
public inline fun <reified T1 : Any, reified T2 : Any, reified T3 : Any, reified T4 : Any, reified T5 : Any> mockMany(
    mode: MockMode = MokkeryCompilerDefaults.mockMode,
    block: ManyMocks5<T1, T2, T3, T4, T5>.() -> Unit = { }
): ManyMocks5<T1, T2, T3, T4, T5> = throw MokkeryPluginNotAppliedException()

public interface ManyMocks2<T1 : Any, T2 : Any>
public interface ManyMocks3<T1 : Any, T2 : Any, T3 : Any> : ManyMocks2<T1, T2>
public interface ManyMocks4<T1 : Any, T2 : Any, T3 : Any, T4 : Any> : ManyMocks3<T1, T2, T3>
public interface ManyMocks5<T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any> : ManyMocks4<T1, T2, T3, T4>

public inline val <reified T : Any> ManyMocks2<T, *>.t1: T get() = this as T
public inline val <reified T : Any> ManyMocks2<*, T>.t2: T get() = this as T
public inline val <reified T : Any> ManyMocks3<*, *, T>.t3: T get() = this as T
public inline val <reified T : Any> ManyMocks4<*, *, *, T>.t4: T get() = this as T
public inline val <reified T : Any> ManyMocks5<*, *, *, *, T>.t5: T get() = this as T