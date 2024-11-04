package dev.mokkery.test

interface ComplexArgsInterface {

    fun callPrimitive(input: Int): Int

    fun callNullable(input: Int?): Int?

    fun callManyPrimitives(a: Int, b: Double): ComplexType

    fun callComplex(input: ComplexType): ComplexType

    fun <T> callArray(array: Array<T>): Array<T>

    fun callIntArray(array: IntArray): IntArray

    fun callNestedArray(array: Array<Array<Int>>): Array<Int>

    fun callDefaults(a: Int = 1, b: Double = 1.5, c: String = "string"): ComplexType

    fun callPrimitiveVarargs(input: Int = 1, vararg inputs: Int): Int

}
