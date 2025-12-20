// LANGUAGE: +ContextParameters

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.annotations.Matcher

fun <T> <!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@receiver:Matcher<!> MokkeryMatcherScope.matcher(): T = any()

fun <T> matcher(<!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@Matcher<!> scope: MokkeryMatcherScope): T = scope.any()

context(<!PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER!>@Matcher<!> scope: MokkeryMatcherScope)
fun <T> matcher(): T = scope.any()
