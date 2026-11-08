import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope

fun MokkeryMatcherScope.compared(other: Any?, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    if (<!ILLEGAL_OPERATOR_USAGE!>matchers<!> === other) return first
    return first
}

fun MokkeryMatcherScope.printed(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    println(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matchers<!>)
    return first
}

fun MokkeryMatcherScope.spreadToNonMemberFunction(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    listOf(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>*matchers<!>)
    return first
}

fun MokkeryMatcherScope.extensionInvoked(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    <!ILLEGAL_MATCHER_IN_NON_MEMBER!>matchers<!>.also { }
    return first
}

fun MokkeryMatcherScope.indexed(@Matcher vararg matchers: Any?): Any? {
    return <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matchers<!>[0]
}

fun MokkeryMatcherScope.methodInvoked(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matchers<!>.iterator()
    return first
}
