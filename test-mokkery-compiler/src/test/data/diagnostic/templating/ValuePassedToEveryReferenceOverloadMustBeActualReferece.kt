import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns
import dev.mokkery.mock

interface Foo {
    fun call(i: Int, d: Double): Int

    suspend fun callSuspend(i: Int, d: Double): Int
}

fun main() {
    val foo = mock<Foo>()
    val ref = foo::call
    val refSuspend = foo::call
    every(<!FUNCTIONAL_PARAM_MUST_BE_REFERENCE!>ref<!>) returns 1
    everySuspend(<!FUNCTIONAL_PARAM_MUST_BE_REFERENCE!>refSuspend<!>) returns 1
}
