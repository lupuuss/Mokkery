package dev.mokkery.context

import dev.drewhamilton.poko.Poko

/**
 * Represents a [function] call with given [args].
 */
@Poko
public class FunctionCall internal constructor(
    public val function: Function,
    public val args: List<CallArgument>
) : MokkeryContext.Element {

    override val key: Key = Key

    public companion object Key : MokkeryContext.Key<FunctionCall>
}

/**
 * Returns [CallArgument.value] of argument at [index] from [FunctionCall.args] and expects that it is an instance of type [T].
 */
public inline fun <reified T> FunctionCall.argValue(index: Int): T = args[index].value as T

/**
 * Return values of all [FunctionCall.args].
 */
public val FunctionCall.argValues: List<Any?>
    get() = args.map(CallArgument::value)
