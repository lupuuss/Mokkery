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
