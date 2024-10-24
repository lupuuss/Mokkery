package dev.mokkery.internal.signature

import dev.mokkery.internal.bestName
import dev.mokkery.context.CallArgument
import dev.mokkery.context.Function

internal interface SignatureGenerator {

    fun generate(name: String, args: List<CallArgument>): String
}

internal fun SignatureGenerator(): SignatureGenerator = SignatureGeneratorImpl()

internal class SignatureGeneratorImpl : SignatureGenerator {

    override fun generate(
        name: String,
        args: List<CallArgument>
    ) =  "$name(${args.joinToString { it.parameter.signature() }})"

    private fun Function.Parameter.signature() = "$name: ${type.bestName()}"
}
