import dev.mokkery.matcher.MokkeryMatcherScope

fun <!MATCHER_SCOPE_MUST_NOT_BE_NULLABLE!>MokkeryMatcherScope?<!>.nullableReceiver(): Int = 1

fun nullableParameter(<!MATCHER_SCOPE_MUST_NOT_BE_NULLABLE!>scope: MokkeryMatcherScope?<!>): Int = 1

context(<!MATCHER_SCOPE_MUST_NOT_BE_NULLABLE!>scope: MokkeryMatcherScope?<!>)
fun nullableContext(): Int = 1

val <!MATCHER_SCOPE_MUST_NOT_BE_NULLABLE!>MokkeryMatcherScope?<!>.nullableProperty: Int
    get() = 1

val nullableLambda = { <!MATCHER_SCOPE_MUST_NOT_BE_NULLABLE!>scope: MokkeryMatcherScope?<!> -> 1 }

fun MokkeryMatcherScope.nonNullReceiver(): Int = 1
