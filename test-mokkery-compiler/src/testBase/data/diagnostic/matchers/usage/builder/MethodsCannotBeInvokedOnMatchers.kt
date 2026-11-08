import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!>.toString()
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>any<Int>()<!> > 1
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>.toString()
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!> > 1
    return matcher
}
