package dev.mokkery.interceptor

/**
 * Represents a point in the interceptors pipeline that allows registering interceptors of [T].
 */
public interface MokkeryHook<T> {

    public fun register(interceptor: T)

    public fun unregister(interceptor: T)
}
