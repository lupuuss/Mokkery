package dev.mokkery.internal.names

import dev.mokkery.internal.Counter

internal interface MokkeryInstanceIdGenerator {

    fun generate(typeName: String): String

    fun extractType(receiver: String): String
}

internal class UniqueMokkeryInstanceIdGenerator(
    private val mocksCounter: Counter
) : MokkeryInstanceIdGenerator {

    override fun generate(typeName: String) = "$typeName(${mocksCounter.next()})"

    override fun extractType(receiver: String) = receiver.substringBefore('(')
}
