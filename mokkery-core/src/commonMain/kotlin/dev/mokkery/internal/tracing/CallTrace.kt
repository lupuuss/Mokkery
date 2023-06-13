package dev.mokkery.internal.tracing

import dev.mokkery.internal.Mokkery

internal data class CallTrace(
    val mokkery: Mokkery,
    val signature: String,
    val args: List<Any?>,
    val orderStamp: Long,
) {

    override fun toString(): String = buildString {
        append(mokkery.mockId)
        append(".")
        append(signature.substringBefore("/"))
        append("(")
        append(args.joinToString())
        append(")")
    }
}

internal infix fun CallTrace.matches(template: CallTemplate): Boolean {
    return signature == template.signature && template.matchers.zip(args).all { (matcher, arg) -> matcher.match(arg) }
}

internal infix fun CallTrace.doesNotMatch(template: CallTemplate): Boolean = matches(template).not()
