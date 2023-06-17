package dev.mokkery.internal.tracing

import dev.mokkery.internal.templating.CallTemplate

internal data class CallTrace(
    val receiver: String,
    val signature: String,
    val args: List<Any?>,
    val orderStamp: Long,
) {

    override fun toString(): String = buildString {
        append(receiver)
        append(".")
        append(signature.substringBefore("/"))
        append("(")
        append(args.joinToString())
        append(")")
    }
}

internal infix fun CallTrace.matches(template: CallTemplate): Boolean {
    return receiver == template.receiver && signature == template.signature && template.matchers.zip(args).all { (matcher, arg) -> matcher.matches(arg) }
}

internal infix fun CallTrace.doesNotMatch(template: CallTemplate): Boolean = matches(template).not()
