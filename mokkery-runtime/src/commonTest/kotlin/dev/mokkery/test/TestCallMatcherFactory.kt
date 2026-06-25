package dev.mokkery.test

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcher

internal class TestCallMatcherFactory(
    private val block: (MokkeryCollection) -> CallMatcher = { TestCallMatcher() }
) : CallMatcher.Factory {
    override fun create(collection: MokkeryCollection): CallMatcher = block(collection)
}
