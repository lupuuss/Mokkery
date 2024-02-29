package dev.mokkery.test

interface TestInterface : BaseInterface {

    var property: String

    fun callWithString(value: String?): Int

    fun callWithPrimitives(i: Int, j: Int = 1): Double

    fun callWithComplex(list: List<String>): List<Int>

    fun Int.callWithExtensionReceiver(): String

    fun callWithVararg(i: Int, vararg args: String): Double

    suspend fun callWithSuspension(i: Int): List<String>

    fun callWithSelf(dependency: TestInterface)

    fun callUnit(): Unit

    fun callNothing(): Nothing

    fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number

    fun <T> callWithArray(array: Array<T>): T

    fun callWithIntArray(array: IntArray): String

    override fun callWithDefault(value: Int): Int = value + 2

    override suspend fun fetchWithDefault(value: Int): Int = value + 2
}
