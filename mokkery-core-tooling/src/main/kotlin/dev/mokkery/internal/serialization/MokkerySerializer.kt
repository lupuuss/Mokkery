package dev.mokkery.internal.serialization

import dev.mokkery.annotations.InternalMokkeryApi

@InternalMokkeryApi
public interface MokkerySerializer<T> {

    public fun serialize(obj: T): String

    public fun deserialize(string: String): T
}
