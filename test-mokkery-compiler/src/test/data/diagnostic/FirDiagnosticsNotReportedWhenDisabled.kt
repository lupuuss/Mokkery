// DISABLE_FIR_DIAGNOSTICS

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.mock

class FinalClass {

    fun method(i: Int): Int = i
}

fun MokkeryMatcherScope.matcher(value: Int): Int {
    val matcher = any<Int>()
    matcher == 2
    return matcher
}

fun main() {
    val foo = mock<FinalClass>()
    every {
        val m = matcher(any())
        m::class
        foo.method(m)
    } returns 1
}
