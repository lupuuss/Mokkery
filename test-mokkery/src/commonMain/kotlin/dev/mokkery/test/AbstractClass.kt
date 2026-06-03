package dev.mokkery.test

abstract class AbstractClassLevel1 : AbstractClassLevel2() {

    abstract fun callComplex(input: ComplexType): ComplexType

    override fun callPrimitive(input: Int): Int = input

    // to be ignored by the compiler
    inline var inlineProperty
        get() = "Ignored inline property"
        set(value) {}

    @Suppress("NOTHING_TO_INLINE")
    inline fun inlineMethod() = "Ignored inline method"

    fun finalMethod() = "Ignored final method"
}


abstract class AbstractClassLevel2 {

    open fun callPrimitive(input: Int): Int = input + 1
}
