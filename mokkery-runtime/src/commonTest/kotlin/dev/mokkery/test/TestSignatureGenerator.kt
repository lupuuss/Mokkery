package dev.mokkery.test

import dev.mokkery.internal.signature.SignatureGenerator
import dev.mokkery.internal.tracing.CallArg

internal class TestSignatureGenerator(
    var calls: (name: String, args: List<CallArg>) -> String = { _, _ -> "call(i: Int)" },
) : SignatureGenerator {

    private val _recordedCalls = mutableListOf<Pair<String, List<CallArg>>>()
    val recordedCalls: List<Pair<String, List<CallArg>>> = _recordedCalls

    fun returns(value: String) {
        calls = { _, _ -> value }
    }
    override fun generate(name: String, args: List<CallArg>): String {
        _recordedCalls.add(name to args)
        return calls(name, args)
    }
}
