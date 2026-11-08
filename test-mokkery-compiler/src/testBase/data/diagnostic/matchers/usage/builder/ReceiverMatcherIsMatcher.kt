import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.added(): Int = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>this<!> + 1

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.printed(): Int {
    println(<!ILLEGAL_MATCHER_IN_NON_MEMBER!>this<!>)
    return this
}

context(_: MokkeryMatcherScope)
fun @receiver:Matcher Int.inLambda(): Int = listOf(1).map { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS!>this<!> }.first()
