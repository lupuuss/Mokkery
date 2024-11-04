package dev.mokkery.test

import dev.mokkery.context.MokkeryContext

data class TestMokkeryContext(
    val value: Any?
) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<TestMokkeryContext>
}
