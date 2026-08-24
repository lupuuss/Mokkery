package dev.mokkery.context

import dev.drewhamilton.poko.Poko
import dev.mokkery.internal.context.MemberFunctions
import dev.mokkery.internal.utils.bestName
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

/**
 * Represents a Kotlin function.
 */
public interface Function {

    public val id: Id
    public val name: String
    public val parameters: List<Parameter>
    public val returnType: KClass<*>

    @JvmInline
    public value class Id(internal val value: Long) {

        override fun toString(): String = "Function.Id(${value.toHexString()})"
    }

    /**
     * Represents a Kotlin function parameter.
     */
    @Poko
    public class Parameter internal constructor(
        public val name: String,
        public val type: KClass<*>,
        public val isVararg: Boolean
    ) {
        override fun toString(): String = "Parameter(" +
                "name='$name', " +
                "type=${type.bestName()}, " +
                "isVararg=$isVararg)"
    }
}

internal fun Function(
    id: Function.Id,
    name: String,
    parameters: List<Function.Parameter>,
    returnType: KClass<*>,
): Function = FunctionImpl(id, name, parameters, returnType)

internal fun MemberFunctions.lazyFunction(id: Function.Id): Function = LazyFunction(this, id)

private class FunctionImpl(
    override val id: Function.Id,
    override val name: String,
    override val parameters: List<Function.Parameter>,
    override val returnType: KClass<*>
) : Function {

    override fun equals(other: Any?): Boolean = sharedEquals(other)

    override fun hashCode(): Int = sharedHashCode()

    override fun toString(): String = sharedToString()
}

private class LazyFunction(
    private val functions: MemberFunctions,
    override val id: Function.Id,
) : Function {

    private val func get() = functions[id]

    override val name: String
        get() = func.name
    override val parameters: List<Function.Parameter>
        get() = func.parameters
    override val returnType: KClass<*>
        get() = func.returnType

    override fun equals(other: Any?): Boolean = sharedEquals(other)

    override fun hashCode(): Int = sharedHashCode()

    override fun toString(): String = sharedToString()
}

private fun Function.sharedEquals(other: Any?): Boolean {
    return other is Function && this.id == other.id
}

private fun Function.sharedHashCode(): Int = id.hashCode()

private fun Function.sharedToString(): String = "Function(id=$id, name='$name', parameters=$parameters, returnType=${returnType.bestName()})"
