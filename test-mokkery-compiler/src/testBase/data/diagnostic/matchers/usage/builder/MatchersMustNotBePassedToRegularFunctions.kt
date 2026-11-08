import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>)
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>)
    <!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>.toHexString()
    <!ILLEGAL_MATCHER_IN_NON_MEMBER!>any<Int>()<!>.toHexString()
    return matcher
}

context(value: Int)
val contextProp: Int get() = value

context(@Matcher matcher: Int)
fun MokkeryMatcherScope.contextMatcher(): Int {
    println(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>contextProp<!>)
    return matcher
}

fun MokkeryMatcherScope.elvisFallback(value: Int?): Int {
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>value ?: any<Int>()<!>)
    return value ?: any()
}
