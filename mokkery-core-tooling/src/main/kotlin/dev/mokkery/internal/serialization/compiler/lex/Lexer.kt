package dev.mokkery.internal.serialization.compiler.lex

import dev.mokkery.internal.serialization.compiler.core.PeekStream
import dev.mokkery.internal.serialization.compiler.core.collectConsumedWhile
import dev.mokkery.internal.serialization.compiler.core.consumedWhile
import kotlin.collections.plusAssign

internal fun interface Lexer {

    fun lex(stream: PeekStream<Char>): List<Token>

    companion object {

        val escapedToSpecialSymbol = mapOf(
            '"' to '"',
            '\'' to '\'',
            '\\' to '\\',
            'n' to '\n',
            't' to '\t',
            'b' to '\b',
            'r' to '\r',
        )

        val specialToEscapedSymbol = escapedToSpecialSymbol
            .entries
            .associate { it.value to it.key }

        val default = compositeExhaustiveLexer(
            whitespaceLexer,
            symbolLexer,
            charToTokenLexer(
                '+' to Token.Operator.Plus,
                '-' to Token.Operator.Minus,
                '|' to Token.Separator,        // comma can't really be used with compiler plugin arguments for now, so | is used as alternative
                ',' to Token.Separator,        // https://youtrack.jetbrains.com/issue/KT-74872/Support-symbols-escaping-in-values-of-plugin-arguments-passed-with-P
                '(' to Token.Parenthesis.Left,
                ')' to Token.Parenthesis.Right,
            ),
            stringToTokenLexer(
                ".." to Token.Operator.Range,
            ),
            stringLiteralLexer,
            numberLiteralLexer,
        )
    }
}

internal fun compositeExhaustiveLexer(vararg lexers: Lexer) = Lexer { stream ->
    val tokens = mutableListOf<Token>()
    do {
        val initialPos = stream.position
        lexers.forEach {
            tokens += it.lex(stream)
        }
        if (initialPos == stream.position) {
            error("Unexpected token at ${stream.position} - \'${stream.peek()}\'!")
        }
    } while (stream.peek() != null)
    tokens
}

internal val whitespaceLexer = Lexer { stream ->
    stream.consumedWhile { it.isWhitespace() }
    emptyList()
}

internal val symbolLexer = Lexer { stream ->
    // symbol must start with letter
    if (stream.peek()?.isJavaIdentifierStart() != true) return@Lexer emptyList()
    var symbol = ""
    while (true) {
        val peek = stream.peek() ?: break
        if (!peek.isJavaIdentifierPart()) break
        stream.consumed()
        symbol += peek
    }
    listOf(Token.Name(symbol))
}

internal fun charToTokenLexer(
    vararg tokens: Pair<Char, Token>,
): Lexer {
    val map = tokens.toMap()
    return Lexer { stream ->
        val peek = stream.peek() ?: return@Lexer emptyList()
        val token = map[peek] ?: return@Lexer emptyList()
        stream.consumed()
        listOf(token)
    }
}

internal fun stringToTokenLexer(
    vararg tokens: Pair<String, Token>,
) = Lexer { stream ->
    val (str, token) = tokens.find { (str) ->
        var i = 0
        str.all { it == stream.peek(i++) }
    } ?: return@Lexer emptyList()
    stream.consumed(str.length)
    listOf(token)
}

internal val stringLiteralLexer = Lexer { stream ->
    val stringChar = '\"'
    if (stream.peek() != stringChar) return@Lexer emptyList()
    stream.consumed()
    var literal = ""
    while (true) {
        val peek = stream.peek() ?: break
        stream.consumed()
        if (peek == '\\') {
            literal += takeSpecialChar(stream)
            continue
        }
        if (peek == stringChar) return@Lexer listOf(Token.StringLiteral(literal))
        literal += peek
    }
    error("Unexpected end of string literal!")
}

internal val numberLiteralLexer = Lexer { stream ->
    if (stream.peek()?.isDigit() != true) return@Lexer emptyList()
    val digits = stream.collectConsumedWhile { it.isDigit() }
    val zeros = digits.takeWhile { it == '0' }
    val number = digits.subList(zeros.size, digits.size).joinToString("").toInt()
    return@Lexer zeros.map { Token.IntLiteral(0) } + Token.IntLiteral(number)
}

private fun takeSpecialChar(stream: PeekStream<Char>): Char {
    val specialPeek = stream.peek() ?: error("Unexpected end of special character sequence!")
    val character = Lexer.escapedToSpecialSymbol[specialPeek]
        ?: error(
            "Unsupported character sequence!" +
                    " Supported characters are ${Lexer.escapedToSpecialSymbol.keys.map { "\\$it" }}"
        )
    stream.consumed()
    return character
}
