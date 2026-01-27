package dev.mokkery.internal.serialization.compiler.ast


internal interface Symbol {
    val name: String
    val type: Type

    fun render(): String
}


internal data class Parameter(
    override val name: String,
    override val type: Type,
    val isVararg: Boolean = false,
): Symbol {
    override fun render(): String = buildString {
        if (isVararg) {
            append("vararg ")
        }
        append(name)
        append(": ")
        if (isVararg) {
            append(type.elementType.render())
        } else {
            append(type.render())
        }
    }
}

internal data class PropertySymbol(
    override val name: String,
    override val type: Type,
    val access: () -> Any?,
) : Symbol {

    override fun render(): String = "${name}: ${type.render()}"
}

internal data class FunctionSymbol(
    override val name: String,
    override val type: Type,
    val parameters: List<Parameter>,
    val body: (Args) -> Any?,
): Symbol {

    override fun render(): String = "${name}(${parameters.joinToString { it.render() }})"

    internal class Args(val evaluations: List<Evaluation>) {

        val values by lazy { evaluations.map { it.evaluate() } }

        inline fun <reified T> arg(index: Int): T = values[index] as T

        inline operator fun <reified T> component1(): T = arg(0)

        inline operator fun <reified T> component2(): T = arg(1)

        inline operator fun <reified T> component3(): T = arg(2)

        inline operator fun <reified T> component4(): T = arg(3)

        inline operator fun <reified T> component5(): T = arg(4)

        inline operator fun <reified T> component6(): T = arg(5)

        inline operator fun <reified T> component7(): T = arg(6)
    }
}
