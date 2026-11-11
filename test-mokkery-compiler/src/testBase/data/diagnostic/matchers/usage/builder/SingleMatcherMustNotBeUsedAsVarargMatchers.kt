import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.materialize

fun MokkeryMatcherScope.spread(@Matcher first: Any?): Any? = or(first, first, <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>*any<Array<Any?>>()<!>)

fun MokkeryMatcherScope.spreadParam(@Matcher first: Any?, @Matcher array: Array<Any?>): Any? {
    return or(first, first, <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>*array<!>)
}

fun MokkeryMatcherScope.spreadVariable(@Matcher first: Any?): Any? {
    val single = any<Array<Any?>>()
    return or(first, first, <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>*single<!>)
}

fun MokkeryMatcherScope.spreadSelectedByIf(flag: Boolean, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    return or(first, first, <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>*if (flag) matchers else any<Array<Any?>>()<!>)
}

fun MokkeryMatcherScope.varargVariableInitialized(flag: Boolean, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    val selected = <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>if (flag) matchers else any<Array<Any?>>()<!>
    return or(first, first, *selected)
}

fun MokkeryMatcherScope.varargVariableReassigned(flag: Boolean, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    var current = matchers
    if (flag) current = <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>any()<!>
    return or(first, first, *current)
}

fun MokkeryMatcherScope.varargDefault(@Matcher first: Any?, @Matcher vararg matchers: Any? = <!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>any()<!>): Any? {
    return or(first, first, *matchers)
}

fun MokkeryMatcherScope.materialized(): Any? {
    materialize(<!SINGLE_MATCHER_IN_MATCHERS_ARRAY!>*any<Array<Any?>>()<!>)
    return any()
}

fun MokkeryMatcherScope.legal(flag: Boolean, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    or(first, first, *matchers)
    or(first, first, *arrayOf(1, 2))
    or(first, first, *if (flag) matchers else arrayOf(1, 2))
    var current = matchers
    if (flag) current = arrayOf(1, 2)
    return or(first, first, *current)
}
