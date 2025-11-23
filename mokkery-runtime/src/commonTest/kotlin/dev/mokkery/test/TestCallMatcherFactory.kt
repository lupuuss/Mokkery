package dev.mokkery.test

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.CallMatcherFactory

internal class TestCallMatcherFactory(
    private val block: (MokkeryCollection) -> CallMatcher = { TestCallMatcher() }
) : CallMatcherFactory {
    override fun create(collection: MokkeryCollection): CallMatcher = block(collection)
}
