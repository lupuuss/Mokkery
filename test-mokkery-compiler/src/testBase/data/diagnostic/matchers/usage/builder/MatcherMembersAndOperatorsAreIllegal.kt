import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.materialize
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or

val String.extLength: Int get() = length

fun MokkeryMatcherScope.propertyRead(@Matcher matcher: String): String {
    println(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>.length)
    return matcher
}

fun MokkeryMatcherScope.varargPropertyRead(@Matcher first: String, @Matcher vararg matchers: String): String {
    println(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matchers<!>.size)
    return first
}

fun MokkeryMatcherScope.extensionPropertyRead(@Matcher matcher: String): String {
    println(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>matcher<!>.extLength)
    return matcher
}

fun MokkeryMatcherScope.safeCall(@Matcher matcher: String?): String? {
    println(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>?.length)
    return matcher
}

fun MokkeryMatcherScope.elvis(@Matcher matcher: String?, @Matcher default: String): String {
    return not(<!ILLEGAL_OPERATOR_USAGE!>matcher<!> ?: default)
}

fun MokkeryMatcherScope.notNullAssertion(@Matcher matcher: String?): String {
    return not(<!ILLEGAL_OPERATOR_USAGE!>matcher<!>!!)
}

fun MokkeryMatcherScope.destructured(@Matcher matcher: Pair<Int, Int>): Pair<Int, Int> {
    val (<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>first<!>, <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>second<!>) = matcher
    println(first + second)
    return matcher
}

fun MokkeryMatcherScope.stringTemplate(@Matcher matcher: Int): Int {
    println("value: $<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>")
    return matcher
}

fun MokkeryMatcherScope.implicitInvoke(@Matcher matcher: () -> Int): () -> Int {
    println(<!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>())
    return matcher
}

fun MokkeryMatcherScope.boundReference(@Matcher matcher: String): String {
    println(<!ILLEGAL_OPERATOR_USAGE!>matcher<!>::length)
    return matcher
}

fun MokkeryMatcherScope.delegated(@Matcher matcher: Lazy<Int>): Lazy<Int> {
    val value by <!ILLEGAL_OPERATOR_USAGE!>matcher<!>
    println(value)
    return matcher
}

fun MokkeryMatcherScope.thrown(condition: Boolean, @Matcher matcher: Exception): Exception {
    if (condition) throw <!ILLEGAL_OPERATOR_USAGE!>matcher<!>
    return matcher
}

fun MokkeryMatcherScope.matcherFunctionReference(@Matcher matcher: Int): Int =
    or(matcher, matcher, *listOf(1, 2).map(<!ILLEGAL_OPERATOR_USAGE!>::gt<!>).toTypedArray())

fun MokkeryMatcherScope.materializerReference(): List<ArgMatcher<Int>> =
    listOf(1, 2).map(<!ILLEGAL_OPERATOR_USAGE!>this::materialize<!>)

class MutableHolder {
    var value: Int = 0
}

fun MokkeryMatcherScope.memberAssignment(@Matcher matcher: MutableHolder): MutableHolder {
    <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>matcher<!>.value = 1
    return matcher
}
