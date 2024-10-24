package dev.mokkery.test

import dev.mokkery.internal.signature.SignatureGenerator
import dev.mokkery.context.CallArgument

internal class TestSignatureGenerator(
    var calls: (name: String, args: List<CallArgument>) -> String = { _, _ -> "call(i: Int)" },
) : SignatureGenerator {

    private val _recordedCalls = mutableListOf<Pair<String, List<CallArgument>>>()
    val recordedCalls: List<Pair<String, List<CallArgument>>> = _recordedCalls

    fun returns(value: String) {
        calls = { _, _ -> value }
    }
    override fun generate(name: String, args: List<CallArgument>): String {
        _recordedCalls.add(name to args)
        return calls(name, args)
    }
}
