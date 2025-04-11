package dev.mokkery.test

import dev.mokkery.context.MokkeryContext

data class TestContextElement(val value: String) : MokkeryContext.Element {

    override val key = Key(value)

    data class Key(val value: String) : MokkeryContext.Key<TestContextElement>
}

