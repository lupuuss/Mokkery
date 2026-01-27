package dev.mokkery.internal.serialization.compiler.ast

import dev.mokkery.internal.serialization.compiler.core.PeekStream
import dev.mokkery.internal.serialization.compiler.core.asPeekStream
import dev.mokkery.internal.serialization.compiler.core.removePrefix
import dev.mokkery.internal.serialization.compiler.core.removeSuffix
import dev.mokkery.internal.serialization.compiler.core.splitBy
import dev.mokkery.internal.serialization.compiler.lex.Token

internal fun interface Parser {

    context(context: ParserContext)
    fun parse(stream: PeekStream<Token>): Expression?

    sealed class Precedence(val level: Int) : Comparable<Precedence> {

        override fun compareTo(other: Precedence): Int = level.compareTo(other.level)

        data object Lowest : Precedence(0)
        data object Infix : Precedence(1)
        data object BinaryOperator : Precedence(2)
        data object Unary : Precedence(3)
        data object Call : Precedence(4)
        data object Primary : Precedence(5)
    }

    companion object {

        val default = compositeParser(
            stringLiteralParser,
            groupingParser,
            binaryOperatorParser,
            infixParser,
            callParser,
            unaryOperatorParser,
        )
    }
}

internal val NoopParser = Parser { error("Noop parser called!") }

internal inline fun parserWith(
    precedence: Parser.Precedence,
    crossinline parser: context(ParserContext)(stream: PeekStream<Token>) -> Expression?,
): Parser = Parser {
    if (parserContext.precedence > precedence) return@Parser null
    parser(it)
}

context(context: ParserContext)
internal inline val parserContext: ParserContext
    get() = context

context(context: ParserContext)
internal inline fun <T> withContext(
    left: Expression? = context.left,
    parser: Parser = context.parser,
    precedence: Parser.Precedence = Parser.Precedence.Lowest,
    block: context(ParserContext)() -> T,
): T = context(ParserContext(parser, precedence, left)) {
    block()
}

context(context: ParserContext)
internal fun Parser.parseUntilExhausted(
    stream: PeekStream<Token>,
): Expression? {
    var left: Expression? = null
    while (stream.peek() != null) {
        val initialPos = stream.position
        withContext(left = left, parser = this) { left = this.parse(stream) }
        if (stream.position == initialPos) error("Parser hanged!")
    }
    return left
}

internal fun compositeParser(vararg parsers: Parser) = Parser { stream ->
    parsers.firstNotNullOfOrNull {
        it.parse(stream)
    }
}

internal val stringLiteralParser = Parser {
    val token = it.peek()
    if (token !is Token.StringLiteral) return@Parser null
    it.consumed()
    Expression.StringLiteral(token.value)
}

internal val groupingParser = Parser { stream ->
    val token = stream.peek()
    if (token != Token.Parenthesis.Left) return@Parser null
    val results = stream.peekUntilClosingParenthesisOrNull() ?: error("Missing closing parenthesis!")
    stream.consumed(results.size)
    val inside = results.drop(1).dropLast(1)
    withContext(left = null, precedence = Parser.Precedence.Lowest) {
        parserContext
            .parser
            .parseUntilExhausted(inside.asPeekStream())
    }
}

internal val callParser = parserWith(Parser.Precedence.Call) { stream ->
    val token = stream.peek()
    if (token !is Token.Name) return@parserWith null
    val id = Identifier(token.value)
    stream.consumed()
    val next = stream.peek()
    if (next != Token.Parenthesis.Left) return@parserWith Expression.Access(id)
    val arguments = stream.peekUntilClosingParenthesisOrNull() ?: error("Missing closing parenthesis")
    val cleansedArguments = arguments
        .removePrefix(Token.Parenthesis.Left)
        .removePrefix(Token.Separator)
        .removeSuffix(Token.Parenthesis.Right)
        .removeSuffix(Token.Separator)
        .splitBy(Token.Separator)
    stream.consumed(arguments.size)
    Expression.Call(
        id = id,
        arguments = withContext(precedence = Parser.Precedence.Lowest) {
            cleansedArguments.map {
                parserContext.parser.parse(it.asPeekStream()) ?: error("Expected an argument")
            }
        }
    )
}

internal val unaryOperatorParser = parserWith(Parser.Precedence.Unary) {
    val token = it.peek()
    if (token !is Token.Operator) return@parserWith null
    it.consumed()
    val id = when (token) {
        Token.Operator.Minus -> Identifier.Minus
        Token.Operator.Plus -> Identifier.Plus
    }
    Expression.UnaryOperator(
        id = id,
        operand = withContext(precedence = Parser.Precedence.Call) {
            parserContext.parser.parse(it) ?: error("Expected an argument")
        }
    )
}

internal val binaryOperatorParser = parserWith(Parser.Precedence.BinaryOperator) {
    val left = parserContext.left ?: return@parserWith null
    val token = it.peek()
    if (token !is Token.Operator) return@parserWith null
    it.consumed()
    val id = when (token) {
        Token.Operator.Minus -> Identifier.Minus
        Token.Operator.Plus -> Identifier.Plus
    }
    Expression.BinaryOperator(
        id = id,
        left = left,
        right = withContext(precedence = Parser.Precedence.Unary, left = null) {
            parserContext.parser.parse(it) ?: error("Expected an argument")
        }
    )
}

internal val infixParser = parserWith(Parser.Precedence.Infix) {
    val left = parserContext.left ?: return@parserWith null
    val token = it.peek()
    if (token !is Token.Name) return@parserWith null
    it.consumed()
    Expression.Infix(
        id = Identifier(token.value),
        left = left,
        right = withContext(precedence = Parser.Precedence.Unary, left = null) {
            parserContext.parser.parse(it) ?: error("Expected an argument")
        }
    )
}

private fun PeekStream<Token>.peekUntilClosingParenthesisOrNull(): List<Token>? {
    val tokens = mutableListOf<Token>()
    var index = 0
    var closing = 0
    while (true) {
        val token = peek(index++) ?: break
        tokens += token
        when (token) {
            Token.Parenthesis.Left -> closing++
            Token.Parenthesis.Right if (--closing == 0) -> return tokens
            else -> Unit
        }
    }
    return null
}
