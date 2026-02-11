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
    every(<!FUNCTION_REFERENCE_INCORRECT_TYPE!>foo::callSuspend<!>) returns 1
    everySuspend(<!FUNCTION_REFERENCE_INCORRECT_TYPE!>foo::call<!>) returns 1
}
