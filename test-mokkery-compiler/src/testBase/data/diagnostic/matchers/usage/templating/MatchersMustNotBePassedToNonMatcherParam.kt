import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gte

interface Foo {
    fun foo(arg: Int): Int
}

fun main() {
    val mock = mock<Foo>()
    every { mock.foo(gte(<!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>any()<!>)) }
    verify { mock.foo(gte(<!MATCHER_PASSED_TO_NON_MATCHER_PARAM!>any()<!>)) }
}
