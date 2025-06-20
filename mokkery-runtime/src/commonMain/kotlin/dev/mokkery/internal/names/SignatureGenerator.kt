package dev.mokkery.internal.names

import dev.mokkery.internal.utils.bestName
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function

internal interface SignatureGenerator {

    fun generate(name: String, args: List<Function.Parameter>): String
}

internal fun SignatureGenerator.generate(name: String, args: List<CallArgument>): String = generate(
    name = name,
    args = args.map { it.parameter }
)

internal fun SignatureGenerator(): SignatureGenerator = SignatureGeneratorImpl()

internal class SignatureGeneratorImpl : SignatureGenerator {

    override fun generate(
        name: String,
        args: List<Function.Parameter>
    ) =  "$name(${args.joinToString { it.signature() }})"

    private fun Function.Parameter.signature() = "$name: ${type.bestName()}"
}
