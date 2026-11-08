import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or

fun MokkeryMatcherScope.passedToSingleParam(@Matcher vararg matchers: Any?): Any? = not(<!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>)

fun MokkeryMatcherScope.passedAsVarargElement(@Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    return or(first, first, <!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>)
}

fun MokkeryMatcherScope.passedViaVariable(@Matcher vararg matchers: Any?): Any? {
    val copy = matchers
    return not(<!VARARG_MATCHER_WITHOUT_SPREAD!>copy<!>)
}

fun <T> MokkeryMatcherScope.passedWithSpread(@Matcher first: T, @Matcher vararg rest: T): T {
    val copy = rest
    or(first, first, *copy)
    return or(first, first, *rest)
}

fun MokkeryMatcherScope.returned(@Matcher vararg matchers: Any?): Array<out Any?> = <!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>

fun MokkeryMatcherScope.returnedViaVariable(@Matcher vararg matchers: Any?): Array<out Any?> {
    val copy = matchers
    return <!VARARG_MATCHER_WITHOUT_SPREAD!>copy<!>
}

fun MokkeryMatcherScope.selectedByIf(flag: Boolean, @Matcher first: Any?, @Matcher vararg matchers: Any?): Any? {
    not(<!VARARG_MATCHER_WITHOUT_SPREAD!>if (flag) matchers else first<!>)
    return <!VARARG_MATCHER_WITHOUT_SPREAD!>if (flag) matchers else first<!>
}

fun <T> MokkeryMatcherScope.selectedByIfAndSpread(
    flag: Boolean,
    alternatives: Array<T>,
    @Matcher first: T,
    @Matcher vararg matchers: T
): T {
    val selected = if (flag) matchers else alternatives
    return or(first, first, *selected)
}

fun MokkeryMatcherScope.usedAsDefault(
    @Matcher vararg matchers: Int,
    @Matcher all: IntArray = <!VARARG_MATCHER_WITHOUT_SPREAD!>matchers<!>
): IntArray = all

fun MokkeryMatcherScope.selectedByElvis(maybe: Array<out Any?>?, @Matcher vararg matchers: Any?): Any? {
    not(<!VARARG_MATCHER_WITHOUT_SPREAD!>maybe ?: matchers<!>)
    return <!VARARG_MATCHER_WITHOUT_SPREAD!>maybe ?: matchers<!>
}
