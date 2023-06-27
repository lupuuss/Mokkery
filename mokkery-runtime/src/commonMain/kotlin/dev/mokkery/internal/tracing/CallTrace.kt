package dev.mokkery.internal.tracing

import dev.mokkery.internal.templating.CallTemplate

internal data class CallTrace(
    val receiver: String,
    val name: String,
    val args: List<CallArg>,
    val orderStamp: Long,
) {

    val signature: String = "$name(${args.joinToString { "${it.name}: ${it.type.simpleName}" }})"

    override fun toString(): String = buildString {
        append(receiver)
        append(".")
        append(name)
        append("(")
        append(args.joinToString { "${it.name} = ${it.value}" })
        append(")")
    }
}

internal infix fun CallTrace.matches(template: CallTemplate): Boolean {
    return receiver == template.receiver && name == template.name && args
        .map { arg -> arg to template.matchers.find { it.name == arg.name } }
        .all { (arg, named) -> named?.matcher?.matches(arg.value) ?: false }
}

internal infix fun CallTrace.doesNotMatch(template: CallTemplate): Boolean = matches(template).not()
