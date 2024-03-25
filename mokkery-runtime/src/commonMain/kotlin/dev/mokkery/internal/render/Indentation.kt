package dev.mokkery.internal.render

internal fun indentationString(size: Int) = CharArray(size) { ' ' }.concatToString()

internal fun String.withIndentation(size: Int) = if (lastOrNull() == '\n') {
    dropLast(1).prependIndent(indentationString(size)).plus('\n')
} else {
    prependIndent(indentationString(size))
}
