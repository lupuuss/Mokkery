package dev.mokkery.test

abstract class TestGenericClass<out T : Any> {
    abstract fun call(value: String): T

    abstract fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number
}
