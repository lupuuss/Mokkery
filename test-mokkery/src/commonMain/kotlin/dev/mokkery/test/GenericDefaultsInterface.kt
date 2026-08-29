package dev.mokkery.test

interface GenericDefaultsInterface<T> {

    val defaultValue: T

    fun call(value: T, other: T? = null, name: String = "name($value)"): String

    fun callWithPropertyDefault(value: T, other: T = defaultValue): String
}
