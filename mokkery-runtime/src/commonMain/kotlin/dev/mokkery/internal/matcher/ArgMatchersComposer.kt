package dev.mokkery.internal.matcher

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher

internal interface ArgMatchersComposer {

    fun compose(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?>
}

internal fun ArgMatchersComposer(): ArgMatchersComposer = ArgMatchersComposerImpl()

private class ArgMatchersComposerImpl : ArgMatchersComposer {
    override fun compose(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        return when {
            arg.isVararg -> composeVarargs(arg, matchers)
            matchers.isEmpty() -> ArgMatcher.Equals(arg.value)
            else -> compose(arg.name, matchers)
        }
    }

    private fun compose(name: String, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        val stack = mutableListOf<ArgMatcher<Any?>>()
        for (it in matchers) {
            if (it !is ArgMatcher.Composite<Any?>) {
                stack += it
                continue
            }
            var composite: ArgMatcher.Composite<Any?> = it
            while (stack.isNotEmpty() && !composite.isFilled()) {
                composite = composite.compose(stack.removeLast())
            }
            composite.assertFilled()
            stack += composite
        }
        return stack.singleOrNull() ?: throw MultipleMatchersForSingleArgException(name, stack)
    }

    private fun composeVarargs(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        val matcher = compose(arg.name, matchers + CompositeVarArgMatcher(arg.value.arrayElementType()))
        require(matcher is CompositeVarArgMatcher)
        val matchersSize = matcher.matchers.size
        val expectedMatchersSize = (arg.value.toListOrNull()?.size ?: 0)
        if (matchersSize != expectedMatchersSize) throw VarargsAmbiguityDetectedException()
        return matcher
    }
}
