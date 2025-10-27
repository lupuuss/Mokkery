package dev.mokkery.context

import dev.drewhamilton.poko.Poko
import kotlin.reflect.KClass

/**
 * Argument for a mocked method call. It is a combination of a [parameter] and [value].
 */
@Poko
public class CallArgument internal constructor(
    public val value: Any?,
    public val parameter: Function.Parameter,
) {

    internal constructor(
        value: Any?,
        name: String,
        type: KClass<*>,
        isVararg: Boolean,
    ) : this(value, Function.Parameter(name, type, isVararg))

    internal fun copy(type: KClass<*>): CallArgument {
        val param = parameter
        return CallArgument(
            value = value,
            parameter = Function.Parameter(
                name = param.name,
                type = type,
                isVararg = param.isVararg
            )
        )
    }
}
