import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or

val MokkeryMatcherScope.gtOne: Int
    get() = gt(1)

val MokkeryMatcherScope.illegalBody: Int
    get() = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>gt(1)<!> + 1

fun MokkeryMatcherScope.passedToMatcher(): Int = or(gtOne, 2)

fun MokkeryMatcherScope.returned(): Int = gtOne

fun MokkeryMatcherScope.storedInVariable(): Int {
    val matcher = gtOne
    return not(matcher)
}

fun MokkeryMatcherScope.arithmetic(): Int = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>gtOne<!> + 1

fun MokkeryMatcherScope.passedToRegularFunction(): Int = maxOf(<!MATCHER_PASSED_TO_NON_MATCHER_IN_MATCHER_BUILDER!>gtOne<!>, 1)

fun MokkeryMatcherScope.nested(): Int = run { <!ILLEGAL_NESTED_FUNCTIONS_MATCHERS_IN_MATCHER_BUILDER!>gtOne<!> }

fun MokkeryMatcherScope.reference(): () -> Int = <!ILLEGAL_MATCHER_REFERENCE!>this::gtOne<!>

context(scope: MokkeryMatcherScope)
val @receiver:Matcher Int.negated: Int
    get() = scope.not(this)

context(scope: MokkeryMatcherScope, @Matcher value: Int)
val negatedContext: Int
    get() = scope.not(value)

context(scope: MokkeryMatcherScope)
val @receiver:Matcher Int.misusedReceiver: Int
    get() = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>this<!> + 1

context(scope: MokkeryMatcherScope, @Matcher value: Int)
val misusedContext: Int
    get() = <!ILLEGAL_MEMBER_ACCESS_ON_MATCHER!>value<!> + 1

fun MokkeryMatcherScope.matcherPassedToReceiver(): Int = gt(1).negated

context(scope: MokkeryMatcherScope, @Matcher value: Int)
fun matcherForwardedToContext(): Int = negatedContext
