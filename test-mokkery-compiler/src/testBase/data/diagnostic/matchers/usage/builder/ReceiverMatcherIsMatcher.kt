import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.added(): Int = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>this<!> + 1

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.printed(): Int {
    println(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>this<!>)
    return this
}

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.inLambda(): Int = listOf(1).map { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>this<!> }.first()
