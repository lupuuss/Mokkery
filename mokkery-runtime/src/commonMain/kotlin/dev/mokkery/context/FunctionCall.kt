package dev.mokkery.context

import dev.mokkery.internal.context.MemberFunctions

/**
 * Represents a [function] call with given [args].
 */
public interface FunctionCall : MokkeryContext.Element {

    public val function: Function
    public val args: List<CallArgument>

    override val key: Key get() = Key

    public companion object Key : MokkeryContext.Key<FunctionCall>
}

/**
 * Returns [CallArgument.value] of argument at [index] from [FunctionCall.args] and expects that it is an instance of type [T].
 */
public inline fun <reified T> FunctionCall.argValue(index: Int): T = this@argValue.argValues[index] as T

/**
 * Return values of all [FunctionCall.args].
 */
public val FunctionCall.argValues: List<Any?>
    get() = when (this) {
        is LazyFunctionCall -> this.argValues
        else -> ArgValuesView(this.args)
    }

internal fun FunctionCall(
    function: Function,
    args: List<CallArgument>,
): FunctionCall = FunctionCallImpl(function, args)

internal fun MemberFunctions.lazyFunctionCall(
    functionId: Function.Id,
    arguments: List<Any?>
): FunctionCall = LazyFunctionCall(this, functionId, arguments)

private class FunctionCallImpl(
    override val function: Function,
    override val args: List<CallArgument>
) : AbstractFunctionCall()

private class ArgValuesView(
    private val args: List<CallArgument>,
) : AbstractList<Any?>() {

    override val size: Int
        get() = args.size

    override fun get(index: Int): Any? = args[index].value
}

private class LazyFunctionCall(
    functions: MemberFunctions,
    functionId: Function.Id,
    val argValues: List<Any?>,
) : AbstractFunctionCall() {

    override val function = functions.lazyFunction(functionId)

    override val args = LazyArgsView(this)
}

private class LazyArgsView(
    private val call: LazyFunctionCall,
) : AbstractList<CallArgument>() {

    override val size: Int
        get() = call.argValues.size

    override fun get(index: Int): CallArgument = LazyCallArgument(call, index)
}

private class LazyCallArgument(
    private val call: LazyFunctionCall,
    private val index: Int,
) : AbstractCallArgument() {

    override val value: Any?
        get() = call.argValues[index]

    override val parameter: Function.Parameter
        get() = call.function.parameters[index]
}

private abstract class AbstractFunctionCall : FunctionCall {

    override fun toString(): String = "FunctionCall(function=$function, args=$args)"

    override fun hashCode(): Int {
        var result = function.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FunctionCall) return false
        if (function != other.function) return false
        if (args != other.args) return false
        return true
    }
}
