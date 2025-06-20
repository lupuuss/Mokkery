package dev.mokkery.test

import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.context.Function

internal class TestSignatureGenerator(
    var calls: (name: String, args: List<Function.Parameter>) -> String = { _, _ -> "call(i: Int)" },
) : SignatureGenerator {

    private val _recordedCalls = mutableListOf<Pair<String, List<Function.Parameter>>>()
    val recordedCalls: List<Pair<String, List<Function.Parameter>>> = _recordedCalls

    fun returns(value: String) {
        calls = { _, _ -> value }
    }

    override fun generate(
        name: String,
        args: List<Function.Parameter>
    ): String {
        _recordedCalls.add(name to args)
        return calls(name, args)
    }
}
