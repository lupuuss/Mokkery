package dev.mokkery.internal.serialization

import dev.mokkery.internal.serialization.compiler.ast.FunctionSymbol
import dev.mokkery.internal.serialization.compiler.ast.Identifier
import dev.mokkery.internal.serialization.compiler.ast.Parameter
import dev.mokkery.internal.serialization.compiler.ast.PropertySymbol
import dev.mokkery.internal.serialization.compiler.ast.SymbolTable
import dev.mokkery.internal.serialization.compiler.ast.Type
import dev.mokkery.internal.serialization.compiler.ast.constSymbolTable
import dev.mokkery.internal.serialization.compiler.ast.parseAndEvaluate
import dev.mokkery.internal.serialization.compiler.ast.plus
import dev.mokkery.internal.serialization.compiler.lex.Lexer
import dev.mokkery.options.AnnotationSelector
import dev.mokkery.options.AnnotationSelector.Companion.all
import dev.mokkery.options.AnnotationSelector.Companion.matches
import dev.mokkery.options.AnnotationSelector.Companion.named
import dev.mokkery.options.AnnotationSelector.Companion.none
import dev.mokkery.options.AnnotationSelectorInternals
import kotlin.error

internal object AnnotationSelectorSerializer : MokkerySerializer<AnnotationSelector> {

    override fun serialize(obj: AnnotationSelector): String = buildString {
        obj.serializeInto(this)
    }

    override fun deserialize(
        string: String
    ): AnnotationSelector = parseAndEvaluate(
        string = string,
        type = AnnotationSelectorType,
        symbolTable = SymbolTable.standard + annotationsRuleSymbolTable
    ) ?: error("Empty expression is not allowed!")

    private fun AnnotationSelector.serializeInto(builder: StringBuilder) {
        when (this) {
            is AnnotationSelectorInternals.Combined -> serializeInto(builder)
            is AnnotationSelectorInternals.Matches -> serializeInto(builder)
            is AnnotationSelectorInternals.Named -> serializeInto(builder)
            is AnnotationSelectorInternals.Minus -> serializeInto(builder)
            AnnotationSelectorInternals.All -> builder.append("all")
            AnnotationSelectorInternals.None -> builder.append("none")
        }
    }

    private fun AnnotationSelectorInternals.Combined.serializeInto(builder: StringBuilder) {
        builder.append("(")
        elements[0].serializeInto(builder)
        for (i in 1..<elements.size) {
            builder.append(' ')
            val element = elements[i]
            if (element is AnnotationSelectorInternals.Minus) {
                builder.append('-')
                builder.append(' ')
                element.selector.serializeInto(builder)
            } else {
                builder.append('+')
                builder.append(' ')
                element.serializeInto(builder)
            }
        }
        builder.append(")")
    }

    private fun AnnotationSelectorInternals.Matches.serializeInto(builder: StringBuilder) {
        val serialized = callStr(
            name = "matches",
            args = listOf(literalStr(regex.pattern)) + regex.options.map { it.name }
        )
        builder.append(serialized)
    }

    private fun AnnotationSelectorInternals.Named.serializeInto(builder: StringBuilder) {
        val serialized = callStr(name = "named", args = this.names.map(::literalStr))
        builder.append(serialized)
    }

    private fun AnnotationSelectorInternals.Minus.serializeInto(builder: StringBuilder) {
        builder.append("-")
        selector.serializeInto(builder)
    }

    private fun literalStr(value: String) = value
        .map { char -> Lexer.specialToEscapedSymbol[char]?.let { "\\$it" } ?: char.toString() }
        .joinToString(separator = "", prefix = "\"", postfix = "\"")

    private fun callStr(name: String, args: List<String>) = args.joinToString(
        prefix = "$name(",
        postfix = ")",
        separator = "|",
    )

    private val AnnotationSelectorType = Type.Simple("AnnotationSelector")

    private val RegexOptionEnumType = Type.Simple("RegexOption")
    private val regexOptionSymbols = RegexOption
        .entries
        .map { PropertySymbol(name = it.name, type = RegexOptionEnumType, access = { it }) }
        .toTypedArray()

    private val annotationsRuleSymbolTable = constSymbolTable(
        *regexOptionSymbols,
        PropertySymbol(name = "all", type = AnnotationSelectorType, access = { all }),
        PropertySymbol(name = "none", type = AnnotationSelectorType, access = { none }),
        FunctionSymbol(
            name = "named",
            type = AnnotationSelectorType,
            parameters = listOf(Parameter(name = "names", type = Type.Array(Type.String), isVararg = true)),
            body = { (names: List<String>) -> named(names = names.toTypedArray()) }
        ),
        FunctionSymbol(
            name = "matches",
            type = AnnotationSelectorType,
            parameters = listOf(
                Parameter(name = "pattern", type = Type.String),
                Parameter(name = "options", type = Type.Array(RegexOptionEnumType), isVararg = true),
            ),
            body = { (regex: String, options: List<RegexOption>) ->
                matches(Regex(pattern = regex, options = options.toSet()))
            }
        ),
        FunctionSymbol(
            name = Identifier.Plus.value,
            type = AnnotationSelectorType,
            parameters = listOf(Parameter(name = "rule", type = AnnotationSelectorType)),
            body = { (rule: AnnotationSelector) -> rule }
        ),
        FunctionSymbol(
            name = Identifier.Minus.value,
            type = AnnotationSelectorType,
            parameters = listOf(Parameter(name = "rule", type = AnnotationSelectorType)),
            body = { (rule: AnnotationSelector) -> -rule }
        ),
        FunctionSymbol(
            name = Identifier.Plus.value,
            type = AnnotationSelectorType,
            parameters = listOf(
                Parameter(name = "left", type = AnnotationSelectorType),
                Parameter(name = "right", type = AnnotationSelectorType)
            ),
            body = { (left: AnnotationSelector, right: AnnotationSelector) -> left + right }
        ),
        FunctionSymbol(
            name = Identifier.Minus.value,
            type = AnnotationSelectorType,
            parameters = listOf(
                Parameter(name = "left", type = AnnotationSelectorType),
                Parameter(name = "right", type = AnnotationSelectorType)
            ),
            body = { (left: AnnotationSelector, right: AnnotationSelector) -> left - right }
        ),
    )
}

