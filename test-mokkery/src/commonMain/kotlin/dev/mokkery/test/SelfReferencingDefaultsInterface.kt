package dev.mokkery.test

interface SelfReferencingDefaultsInterface {

    val defaultName: String

    fun defaultMail(i: Int): String

    fun callWithPropertyDefault(i: Int, name: String = defaultName): String

    fun callWithFunctionDefault(i: Int, mail: String = defaultMail(i)): String

    fun overloaded(a: Int, b: Int): String

    fun overloaded(x: String, y: String = overloaded(1, 2)): String

    fun mixedDefaults(i: Int, name: String = defaultName, tag: String = "tag"): String

    fun sameParameterNames(a: Int, b: Int): String

    fun sameParameterNames(a: String, b: String = sameParameterNames(1, 2)): String
}
