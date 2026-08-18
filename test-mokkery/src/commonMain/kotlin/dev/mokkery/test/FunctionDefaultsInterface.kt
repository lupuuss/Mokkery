package dev.mokkery.test

interface FunctionDefaultsInterface {

    fun call(i: Int, name: String = "name($i)", mail: String = "name$i@mail.com"): String

    suspend fun callSuspend(i: Int, name: String = "name($i)", mail: String = "name$i@mail.com"): String

    fun Int.callExtension(name: String = "name($this)", mail: String = "name$this@mail.com"): String
}
