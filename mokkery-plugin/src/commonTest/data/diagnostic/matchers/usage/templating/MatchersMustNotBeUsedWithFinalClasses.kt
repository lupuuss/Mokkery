import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any

interface Foo {

    fun foo(i: Int)
}

fun main() {
    val mock = object : Foo {
        override fun foo(i: Int) = TODO()
    }
    every {
        mock.foo(<!MATCHER_USED_WITH_FINAL_CLASS!>any()<!>)
    }
    verify {
        mock.foo(<!MATCHER_USED_WITH_FINAL_CLASS!>any()<!>)
    }
}
