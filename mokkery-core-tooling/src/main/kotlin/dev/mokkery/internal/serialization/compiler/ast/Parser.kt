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

    data class Precedence(val level: Int) : Comparable<Precedence> {

        fun next(): Precedence = Precedence((level + 1).coerceAtMost(Highest.level))

        override fun compareTo(other: Precedence): Int = level.compareTo(other.level)

        companion object {

            val Lowest = Precedence(0)
            val Infix = Precedence(1)
            val Sum = Precedence(2)
            val Product = Precedence(3)
            val Prefix = Precedence(4)
            val Call = Precedence(5)
            val Highest = Precedence(6)
        }
    }

    companion object {

        val default = compositeParser(
            literalsParser,
            groupingParser,
            binaryOperatorParser,
            infixParser,
            callParser,
            unaryOperatorParser,
        )
    }
}

internal val NoopParser = Parser { error("Noop parser called!") }

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
internal fun Parser.parseAll(
    stream: PeekStream<Token>,
): Expression? {
    var left: Expression? = null
    while (stream.peek() != null) {
        val initialPos = stream.position
        withContext(left = left, parser = this) { left = this.parse(stream) }
        if (stream.position == initialPos) error("Unexpected token ${stream.peek()?.toString()}")
    }
    return left
}

internal fun compositeParser(vararg parsers: Parser) = Parser { stream ->
    parsers.firstNotNullOfOrNull {
        it.parse(stream)
    }
}

internal val literalsParser = Parser {
    val result = when (val token = it.peek()) {
        is Token.IntLiteral -> Expression.IntLiteral(token.value)
        is Token.StringLiteral -> Expression.StringLiteral(token.value)
        else -> return@Parser null
    }
    it.consumed()
    result
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
            .parseAll(inside.asPeekStream())
    }
}

internal val callParser = Parser { stream ->
    if (parserContext.precedence > Parser.Precedence.Call) return@Parser null
    val token = stream.peek()
    if (token !is Token.Name) return@Parser null
    val id = Identifier(token.value)
    stream.consumed()
    val next = stream.peek()
    if (next != Token.Parenthesis.Left) return@Parser Expression.Access(id)
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
                parserContext.parser.parseAll(it.asPeekStream()) ?: error("Expected an argument")
            }
        }
    )
}

internal val unaryOperatorParser = Parser {
    val token = it.peek()
    if (token !is Token.Operator) return@Parser null
    val id = when (token) {
        Token.Operator.Minus -> Identifier.Minus
        Token.Operator.Plus -> Identifier.Plus
        else -> return@Parser null
    }
    it.consumed()
    Expression.UnaryOperator(
        id = id,
        operand = withContext(precedence = Parser.Precedence.Prefix.next()) {
            parserContext.parser.parse(it) ?: error("Expected an argument")
        }
    )
}

internal val binaryOperatorParser = Parser {
    val left = parserContext.left ?: return@Parser null
    val token = it.peek()
    if (token !is Token.Operator) return@Parser null
    val (id, precedence) = when (token) {
        Token.Operator.Minus -> Identifier.Minus to Parser.Precedence.Sum
        Token.Operator.Plus -> Identifier.Plus to Parser.Precedence.Sum
        Token.Operator.Range -> Identifier.Range to Parser.Precedence.Infix
    }
    if (parserContext.precedence > precedence) return@Parser null
    it.consumed()
    Expression.BinaryOperator(
        id = id,
        left = left,
        right = withContext(precedence = precedence.next(), left = null) {
            parserContext.parser.parse(it) ?: error("Expected an argument")
        }
    )
}

internal val infixParser = Parser {
    val precedence = Parser.Precedence.Infix
    if (parserContext.precedence > precedence) return@Parser null
    val left = parserContext.left ?: return@Parser null
    val token = it.peek()
    if (token !is Token.Name) return@Parser null
    it.consumed()
    Expression.Infix(
        id = Identifier(token.value),
        left = left,
        right = withContext(precedence = precedence.next(), left = null) {
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
