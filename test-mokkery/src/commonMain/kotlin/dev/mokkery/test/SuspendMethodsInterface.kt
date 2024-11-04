package dev.mokkery.test

interface SuspendMethodsInterface {

    suspend fun callUnit(unit: Unit)

    suspend fun callNothing(input: Int): Nothing

    suspend fun callPrimitive(input: Int): Int

    suspend fun callComplex(input: ComplexType): ComplexType

    suspend fun callIntArray(array: IntArray): IntArray

    suspend fun callArray(array: Array<ComplexType>): Array<ComplexType>

    suspend fun callResult(result: Result<Int>): Result<Int>

    suspend fun callOverloaded(input: Int): Int

    suspend fun callOverloaded(input: Double): Double

    suspend fun callOverloaded(input: String): String

    suspend fun callOverloaded(input: ComplexType): ComplexType

    suspend fun callPrimitiveWithDefaults(complexInput: ComplexType = ComplexType, primitiveInput: Int = 1): Int

    suspend fun callComplexWithDefaults(complexInput: ComplexType = ComplexType, primitiveInput: Int = 1): ComplexType

    suspend fun Int.callPrimitiveExtension(): Int

    suspend fun ComplexType.callComplexExtension(): ComplexType
}
