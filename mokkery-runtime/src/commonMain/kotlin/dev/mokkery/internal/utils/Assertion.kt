package dev.mokkery.internal.utils

internal fun failAssertion(block: StringBuilder.() -> Unit): Nothing {
    throw AssertionError(StringBuilder().apply(block).toString())
}
