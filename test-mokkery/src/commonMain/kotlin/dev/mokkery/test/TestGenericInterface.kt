package dev.mokkery.test

interface TestGenericInterface<T : Comparable<T>> {

    val value: T

    fun call(value: T): Boolean

    fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number

    suspend fun <T> callSuspendGeneric(value: T): T where T : Comparable<T>, T : Number

    fun List<T>.extension(): String

    fun <T> List<Comparable<T>>.genericExtension(): String

    val List<T>.listSize: Int

    val <T> List<T>.genericListSize: Int
}
