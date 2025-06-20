package dev.mokkery.test

interface FunctionDefaultsInterface {

    fun call(i: Int, name: String = "name($i)", mail: String = "name$i@mail.com"): String
}
