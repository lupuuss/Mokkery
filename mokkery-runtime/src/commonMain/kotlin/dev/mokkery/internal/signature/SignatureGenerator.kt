package dev.mokkery.internal.signature

import dev.mokkery.internal.bestName
import dev.mokkery.internal.tracing.CallArg

internal interface SignatureGenerator {
    fun generate(name: String, args: List<CallArg>): String
}

internal fun SignatureGenerator(): SignatureGenerator = SignatureGeneratorImpl()

internal class SignatureGeneratorImpl : SignatureGenerator {
    override fun generate(
        name: String,
        args: List<CallArg>
    ) =  "$name(${args.joinToString { "${it.name}: ${it.type.bestName()}" }})"

}
