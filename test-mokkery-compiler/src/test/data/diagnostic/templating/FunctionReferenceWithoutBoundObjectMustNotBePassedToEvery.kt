import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.answering.returns

interface Foo {
    fun call(i: Int, d: Double): Int

    suspend fun callSuspend(i: Int, d: Double): Int
}

fun main() {
    every(<!FUNCTION_REFERENCE_NOT_BOUND!>Foo::call<!>) returns 1
    everySuspend(<!FUNCTION_REFERENCE_NOT_BOUND!>Foo::callSuspend<!>) returns 1
}
