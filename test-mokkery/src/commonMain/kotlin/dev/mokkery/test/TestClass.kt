package dev.mokkery.test

abstract class TestClass(arg: String = "0") {

    abstract val property: Int

    abstract fun call(): String
}
