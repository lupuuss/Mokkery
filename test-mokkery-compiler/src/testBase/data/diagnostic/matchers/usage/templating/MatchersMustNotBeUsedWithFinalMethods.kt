import dev.mokkery.mock
import dev.mokkery.every
import dev.mokkery.verify
import dev.mokkery.matcher.any
import dev.mokkery.templating.ext

class Foo {
    var prop: Int = 0

    fun foo(arg: Int): Int = arg

    val Int.extProp: Int get() = this
}

fun main() {
    val mock = Foo()
    every {
        mock.prop = <!MATCHER_USED_WITH_FINAL_MEMBER!>any()<!>
    }
    every {
        mock.foo(<!MATCHER_USED_WITH_FINAL_MEMBER!>any()<!>)
    }
    verify {
        mock.foo(<!MATCHER_USED_WITH_FINAL_MEMBER!>any()<!>)
    }
    every {
        mock.ext { <!MATCHER_USED_WITH_FINAL_MEMBER!>any<Int>()<!>.extProp }
    }
}
