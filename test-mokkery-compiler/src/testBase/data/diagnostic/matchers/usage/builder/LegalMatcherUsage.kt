import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or

fun <T> MokkeryMatcherScope.passedToMatcher(@Matcher matcher: T): T = not(matcher)

fun <T> MokkeryMatcherScope.returned(@Matcher matcher: T): T = matcher

fun <T> MokkeryMatcherScope.varargSpread(@Matcher first: T, @Matcher vararg rest: T): T = or(first, first, *rest)

fun MokkeryMatcherScope.mixedWithLiterals(@Matcher matcher: Int): Int = or(matcher, 1, gt(2), not(any()))

fun MokkeryMatcherScope.nullLiteral(@Matcher matcher: Int?): Int? = or(matcher, null)

fun MokkeryMatcherScope.nonMatcherDefault(@Matcher matcher: Int, value: Int = 1): Int = or(matcher, value)

fun <T> MokkeryMatcherScope.matcherDefault(@Matcher matcher: T, @Matcher other: T = matcher): T = or(matcher, other)

fun <T> MokkeryMatcherScope.variables(@Matcher matcher: T, other: Any?): T {
    val copy = matcher
    var current = copy
    current = not(current)
    return if (other === null) current else matcher
}

fun <T> MokkeryMatcherScope.reassignedInLoop(@Matcher matcher: T): T {
    var result = matcher
    for (i in 0 until 3) result = not(result)
    return result
}

fun MokkeryMatcherScope.selectedByIf(flag: Boolean, @Matcher matcher: Int): Int = if (flag) matcher else not(matcher)

fun MokkeryMatcherScope.selectedByWhen(flag: Boolean, @Matcher matcher: Int): Int = when {
    flag -> matcher
    else -> 1
}

fun MokkeryMatcherScope.lambdaWithoutMatchers(value: Int): Int = matches {
    it > value && listOf(1).map { x -> x + it }.isNotEmpty()
}

fun MokkeryMatcherScope.materializedBeforeLambda(@Matcher matcher: Int): Int {
    val materialized = materialize(matcher)
    return matches { materialized.matches(it) }
}

fun MokkeryMatcherScope.objectWithoutMatchers(@Matcher matcher: Int): Int {
    val helper = object {
        fun twice(x: Int) = x * 2
    }
    return or(matcher, helper.twice(1))
}

fun MokkeryMatcherScope.tryCatchWithoutMatchers(@Matcher matcher: Int): Int {
    try {
        println("value")
    } catch (e: Exception) {
        println(e)
    }
    return matcher
}

context(scope: MokkeryMatcherScope)
fun <T> @receiver:Matcher T.receiverMatcher(@Matcher other: T): T = scope.or(this, other)

fun MokkeryMatcherScope.literalAndMatcherDefaults(@Matcher first: Int = 5, @Matcher second: Int = gt(1)): Int = or(first, second)

fun MokkeryMatcherScope.explicitScopeReceiver(): Int = this.not(this.any())

inline fun <reified T> MokkeryMatcherScope.inlineBuilder(@Matcher matcher: T): T = not(matcher)

fun MokkeryMatcherScope.nonMatcherOperations(value: Int?, pair: Pair<Int, Int>, callback: (Int) -> Int): Int {
    val (first, second) = pair
    val text = "$first ${value ?: second} ${value!!} ${callback(first)}"
    val lazyValue by lazy { text.length }
    val lengths = listOf(text).map(String::length)
    if (lengths.isEmpty()) throw IllegalStateException(text)
    return or(lazyValue, first)
}

fun MokkeryMatcherScope.matcherOnRightOfElvis(value: Int?): Int = value ?: any()

class MatcherBuilderHolder(private val value: Int) {
    val Int.doubled: Int get() = this * 2

    fun MokkeryMatcherScope.memberBuilder(@Matcher matcher: Int): Int = or(matcher, this@MatcherBuilderHolder.value.doubled)
}

fun MokkeryMatcherScope.expressionShapes(flag: Boolean, @Matcher matcher: Int): Int {
    val parenthesized = (matcher)
    val labeled = label@ not(matcher)
    val selected = when (flag) {
        true -> parenthesized
        false -> labeled
    }
    return or(second = selected, first = (matcher))
}

context(@Matcher matcher: Int)
fun MokkeryMatcherScope.contextOfParameter(): Int = not(contextOf<Int>())
