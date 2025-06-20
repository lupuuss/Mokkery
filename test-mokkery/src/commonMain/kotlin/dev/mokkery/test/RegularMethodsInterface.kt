package dev.mokkery.test

interface RegularMethodsInterface {

    fun callUnit(unit: Unit)

    fun callNothing(input: Int): Nothing

    fun callPrimitive(input: Int): Int

    fun callComplex(input: ComplexType): ComplexType

    fun callIntArray(array: IntArray): IntArray

    fun callArray(array: Array<ComplexType>): Array<ComplexType>

    fun callResult(result: Result<Int>): Result<Int>

    fun callOverloaded(input: Int): Int

    fun callOverloaded(input: Double): Double

    fun callOverloaded(input: String): String

    fun callOverloaded(input: ComplexType): ComplexType

    fun callPrimitiveWithDefaults(complexInput: ComplexType = ComplexType, primitiveInput: Int = 1): Int

    fun callComplexWithDefaults(complexInput: ComplexType = ComplexType, primitiveInput: Int = 1): ComplexType

    fun callWithDependantDefaults(complex: ComplexType, name: String = complex.toString()): String

    fun Int.callPrimitiveExtension(): Int

    fun ComplexType.callComplexExtension(): ComplexType
}
