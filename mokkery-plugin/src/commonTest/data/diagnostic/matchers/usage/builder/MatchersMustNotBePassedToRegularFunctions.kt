import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>matcher<!>)
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>any<Int>()<!>)
    <!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>matcher<!>.toHexString()
    <!ILLEGAL_MATCHER_IN_NON_MEMBER_FUNCTION!>any<Int>()<!>.toHexString()
    return matcher
}
