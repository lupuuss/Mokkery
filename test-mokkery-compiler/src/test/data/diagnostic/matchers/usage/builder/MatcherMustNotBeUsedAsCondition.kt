import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

fun MokkeryMatcherScope.matcherIf(): Boolean {
    val matcher = any<Boolean>()
    if (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
    if (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
    return matcher
}

fun MokkeryMatcherScope.matcherWhenBranch(): Boolean {
    val matcher = any<Boolean>()
    when {
        <!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!> -> Unit
        <!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!> -> Unit
        else -> Unit
    }
    return matcher
}

fun MokkeryMatcherScope.matcherLoopConditon(): Boolean {
    val matcher = any<Boolean>()
    while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>) Unit
    while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>) Unit
    do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>matcher<!>)
    do { } while (<!ILLEGAL_MATCHER_IN_CONDITION!>any<Boolean>()<!>)
    return matcher
}
