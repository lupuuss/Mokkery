package dev.mokkery.context

import kotlin.hashCode

/**
 * Argument for a mocked method call. It is a combination of a [parameter] and [value].
 */
public interface CallArgument {
    public val value: Any?
    public val parameter: Function.Parameter
}

internal fun CallArgument(
    value: Any?,
    parameter: Function.Parameter
): CallArgument = CallArgumentImpl(value, parameter)

internal abstract class AbstractCallArgument : CallArgument {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CallArgument) return false
        if (this.value != other.value) return false
        if (this.parameter != other.parameter) return false
        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + parameter.hashCode()
        return result
    }

    override fun toString(): String = "CallArgument(value=$value, parameter=$parameter)"
}

private class CallArgumentImpl(
    override val value: Any?,
    override val parameter: Function.Parameter
) : AbstractCallArgument()
