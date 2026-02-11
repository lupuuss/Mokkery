package dev.mokkery.internal.serialization.compiler.ast

internal sealed interface Type {

    fun render(): String

    data class Array(val elementType: Type) : Type {

        override fun render(): String = "Array<${elementType.render()}>"

        override fun toString(): String = "#Array<$elementType>"
    }

    data class Simple(val name: String) : Type {

        override fun render(): String = name

        override fun toString(): String = "#$name"
    }

    companion object {
        val String = Simple("String")
        val Int = Simple("Int")
        val IntRange = Simple("IntRange")
    }
}

internal val Type.elementType: Type
    get() = (this as Type.Array).elementType
