package dev.mokkery.test

interface TestDependency {

    var property: String

    fun callWithPrimitives(i: Int): Double

    fun Int.callWithExtensionReceiver(): String

    fun callWithVararg(i: Int, vararg args: String): Pair<Double, Double>

    suspend fun callWithSuspension(i: Int): List<String>

    fun callUnit(): Unit

    fun callNothing(): Nothing
}
