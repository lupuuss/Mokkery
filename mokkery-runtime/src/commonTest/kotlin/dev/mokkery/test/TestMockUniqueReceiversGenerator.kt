package dev.mokkery.test

import dev.mokkery.internal.MockUniqueReceiversGenerator

class TestMockUniqueReceiversGenerator(
    var extractTypeCalls: (String) -> String = { it }
) : MockUniqueReceiversGenerator {

    private val _recordedExtractTypeCalls = mutableListOf<String>()
    val recordedExtractTypeCalls: List<String> = _recordedExtractTypeCalls

    override fun generate(typeName: String): String = error("Not implemented for tests!")

    override fun extractType(receiver: String): String {
        _recordedExtractTypeCalls += receiver
        return extractTypeCalls(receiver)
    }

}
