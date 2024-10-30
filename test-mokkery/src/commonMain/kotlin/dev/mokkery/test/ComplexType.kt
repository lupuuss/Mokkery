package dev.mokkery.test

interface ComplexType {

    val id: String

    companion object : ComplexType {

        override val id: String
            get() = "ComplexType.Companion"

        operator fun invoke(id: String): ComplexType = ComplexTypeImpl(id)
    }
}

private data class ComplexTypeImpl(override val id: String) : ComplexType
