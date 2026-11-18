import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import kotlin.reflect.KClass

fun MokkeryMatcherScope.matcherUsingClass(): KClass<*> {
    return <!ILLEGAL_OPERATOR_USAGE!>any<Int>()<!>::class
}

fun MokkeryMatcherScope.matcherUsingEquals(): Boolean {
    return <!ILLEGAL_OPERATOR_USAGE!>any<Int>()<!> == 1
}

fun MokkeryMatcherScope.matcherUsingRef(): Boolean {
    return <!ILLEGAL_OPERATOR_USAGE!>any<String>()<!> === ""
}

fun MokkeryMatcherScope.matcherUsingClassVar(): KClass<*> {
    val matcher = any<Int>()
    return <!ILLEGAL_OPERATOR_USAGE!>matcher<!>::class
}

fun MokkeryMatcherScope.matcherUsingEqualsVar(): Boolean {
    val matcher = any<Int>()
    return <!ILLEGAL_OPERATOR_USAGE!>any<Int>()<!> == 1
}

fun MokkeryMatcherScope.matcherUsingRefVar(): Boolean {
    val matcher = any<String>()
    return <!ILLEGAL_OPERATOR_USAGE!>matcher<!> === ""
}
