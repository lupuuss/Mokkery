package dev.mokkery.test

interface BaseInterface {

    val baseInterfaceProperty: String

    val <R> R.baseInterfaceGenericProperty: R
        get() = this

    fun baseInterfaceMethod()

    fun callWithDefault(value: Int): Int = value + 1

    fun callWithDefaultNoOverride(): Int = 10

    suspend fun fetchWithDefault(value: Int): Int = value + 1

    fun <R> baseCallWithGeneric(value: R): R = value
}
