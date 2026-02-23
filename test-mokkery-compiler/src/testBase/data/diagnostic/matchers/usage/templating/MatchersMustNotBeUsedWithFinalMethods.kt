import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

class Foo {
    fun foo(arg: Int): Int = arg
}

fun main() {
    val mock = Foo()
    every {
        mock.foo(<!MATCHER_USED_WITH_FINAL_METHOD!>any()<!>)
    }
    verify {
        mock.foo(<!MATCHER_USED_WITH_FINAL_METHOD!>any()<!>)
    }
}
