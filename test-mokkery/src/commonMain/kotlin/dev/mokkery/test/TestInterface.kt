package dev.mokkery.test

interface TestInterface : BaseInterface {

    var property: String
        get() = "Default value"
        set(_) = Unit

    var Int.extProperty: String
        get() = "$this - default property with receiver"
        set(_) = Unit

    fun callWithString(value: String?): Int

    fun callWithPrimitives(i: Int, j: Int = 1): Double

    fun callWithComplex(list: List<String>): List<Int>

    fun Int.callWithExtensionReceiver(): String

    fun Int.callWithExtensionReceiverDefault(): String = "$this - default method with receiver"

    fun callWithVararg(i: Int, vararg args: String): Double

    suspend fun callWithSuspension(i: Int, default: Boolean = true): List<String>

    suspend fun callPrimitiveWithSuspension(i: Int, default: Boolean = true): Int

    suspend fun <T> callGenericWithSuspension(value: T, default: Boolean = true): T

    suspend fun callUnitWithSuspension(i: Int)

    fun callWithSelf(dependency: TestInterface)

    fun callUnit(): Unit

    fun callNothing(): Nothing

    fun callWithPrimitiveResult(value: Result<Int>): Result<Int>

    fun callWithComplexResult(value: Result<List<Int>>): Result<List<Int>>

    suspend fun <T> callWithResult(value: Result<T>): Result<T> = value

    fun callWithPrimitiveValueClass(value: PrimitiveValueClass): PrimitiveValueClass

    fun callWithComplexValueClass(value: ValueClass<List<String>>): ValueClass<List<String>>

    fun <T> callGeneric(value: T, default: Int = 1): T where T : Comparable<T>, T : Number

    fun <T> callWithArray(array: Array<T>): T = array[0]

    fun callWithIntArray(array: IntArray): String

    override fun callWithDefault(value: Int): Int = value + 2

    override suspend fun fetchWithDefault(value: Int): Int = value + 2
}
