package dev.mokkery.test

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

fun <T> MokkeryMatcherScope.externalMatcher(value: T): T = matches { it == value }
