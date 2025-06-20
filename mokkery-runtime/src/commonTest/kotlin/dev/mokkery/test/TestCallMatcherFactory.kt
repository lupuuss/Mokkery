package dev.mokkery.test

import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.CallMatcherFactory
import dev.mokkery.internal.MocksCollection

internal class TestCallMatcherFactory(
    private val block: (MocksCollection) -> CallMatcher = { TestCallMatcher() }
) : CallMatcherFactory {
    override fun create(mocks: MocksCollection): CallMatcher = block(mocks)
}
