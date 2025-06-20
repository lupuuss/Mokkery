package dev.mokkery.test

import dev.mokkery.matcher.ArgMatchersScope

fun <T> ArgMatchersScope.externalMatcher(value: T): T = matches { it == value }
