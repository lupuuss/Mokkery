package dev.mokkery.internal.context

internal object IgnoringMocksRegistry : MocksRegistry {

    override val mocks = emptySet<Any>()

    override fun register(mock: Any) = Unit
}
