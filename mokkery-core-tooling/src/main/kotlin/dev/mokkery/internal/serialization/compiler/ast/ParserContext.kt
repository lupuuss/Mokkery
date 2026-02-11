package dev.mokkery.internal.serialization.compiler.ast

internal data class ParserContext(
    val parser: Parser,
    val precedence: Parser.Precedence,
    val left: Expression?
) {

    companion object {
        val empty = ParserContext(NoopParser, Parser.Precedence.Lowest, null)
    }
}
