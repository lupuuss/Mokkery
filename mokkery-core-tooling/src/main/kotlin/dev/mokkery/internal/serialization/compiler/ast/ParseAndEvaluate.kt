package dev.mokkery.internal.serialization.compiler.ast

import dev.mokkery.internal.serialization.compiler.core.asPeekStream
import dev.mokkery.internal.serialization.compiler.lex.Lexer

internal inline fun <reified T> parseAndEvaluate(
    string: String,
    type: Type,
    symbolTable: SymbolTable
): T? {
    try {
        val tokens = Lexer.default.lex(string.asPeekStream())
        val expression = context(ParserContext.empty) {
            Parser.default.parseAll(tokens.asPeekStream())
        }  ?: return null
        val evaluation = EvaluationFactory(symbolTable)
            .createFrom(expression)
        if (evaluation.type != type) {
            error("Expected ${type.render()} type but got ${evaluation.type.render()}")
        }
        return evaluation.evaluate() as T
    } catch (e: Throwable) {
        throw IllegalArgumentException("Failed while evaluating expression [$string]", e)
    }
}
