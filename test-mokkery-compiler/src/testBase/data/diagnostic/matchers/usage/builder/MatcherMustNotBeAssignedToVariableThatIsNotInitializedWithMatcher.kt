import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    var foo: Int = 0
    <!VARIABLE_NOT_A_MATCHER!>foo = any<Int>()<!>
    return foo
}
