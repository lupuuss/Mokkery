import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>)
    listOf(<!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>)
    <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>matcher<!>.toHexString()
    <!MATCHER_PASSED_TO_NON_MEMBER_FUNCTION!>any<Int>()<!>.toHexString()
    return matcher
}
