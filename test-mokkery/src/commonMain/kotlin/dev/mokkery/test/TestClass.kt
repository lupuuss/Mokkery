package dev.mokkery.test

abstract class TestClass(arg: String = "0"): BaseClass() {

    abstract val property: Int

    abstract fun call(): String

    abstract fun <T> callGeneric(value: T): T where T : Comparable<T>, T : Number

    // to be ignored by the compiler
    inline val inlineProperty get() = "Ignored inline property"
    inline fun inlineMethod() = "Ignored inline method"
    fun finalMethod() = "Ignored final method"
}
