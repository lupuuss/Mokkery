package dev.mokkery.internal

internal interface MockUniqueReceiversGenerator {

    fun generate(typeName: String): String

    fun extractType(receiver: String): String

    companion object : MockUniqueReceiversGenerator {
        override fun generate(typeName: String) = "$typeName(${Counter.mocks.next()})"

        override fun extractType(receiver: String) = receiver.substringBefore('(')
    }
}
