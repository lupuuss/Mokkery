import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcher(): Int {
    val matcher = any<Int>()
    listOf(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>matcher<!>)
    listOf(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>any<Int>()<!>)
    <!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>matcher<!>.toHexString()
    <!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>any<Int>()<!>.toHexString()
    return matcher
}

context(value: Int)
val contextProp: Int get() = value

context(@Matcher matcher: Int)
fun MokkeryMatcherScope.contextMatcher(): Int {
    println(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>contextProp<!>)
    return matcher
}

fun MokkeryMatcherScope.elvisFallback(value: Int?): Int {
    listOf(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>value ?: any<Int>()<!>)
    return value ?: any()
}
