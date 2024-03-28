package dev.mokkery.test

import dev.mokkery.internal.names.NameShortener

class TestNameShortener(
    var calls: (Set<String>) -> Map<String, String> = { names -> names.associateWith { it } }
) : NameShortener {

    private val _recordedCalls = mutableListOf<Set<String>>()
    val recordedCalls: List<Set<String>> = _recordedCalls

    fun returns(value: Map<String, String>) {
        calls = { value }
    }

    override fun shorten(names: Set<String>): Map<String, String> {
        _recordedCalls += names
        return calls(names)
    }
}
