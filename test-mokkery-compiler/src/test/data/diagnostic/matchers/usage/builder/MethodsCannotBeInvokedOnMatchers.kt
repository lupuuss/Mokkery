import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    <!ILLEGAL_METHOD_INVOCATION_ON_MATCHER!>any<Int>()<!>.toString()
    <!ILLEGAL_METHOD_INVOCATION_ON_MATCHER!>any<Int>()<!> > 1
    <!ILLEGAL_METHOD_INVOCATION_ON_MATCHER!>matcher<!>.toString()
    <!ILLEGAL_METHOD_INVOCATION_ON_MATCHER!>matcher<!> > 1
    return matcher
}
