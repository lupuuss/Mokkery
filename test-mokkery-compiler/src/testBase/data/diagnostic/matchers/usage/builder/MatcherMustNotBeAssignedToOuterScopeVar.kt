import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

var foo: Int = 0

fun MokkeryMatcherScope.matcher(): Int {
    <!VARIABLE_OUT_OF_SCOPE_IN_MATCHER_BUILDER!>foo = any<Int>()<!>
    return foo
}
