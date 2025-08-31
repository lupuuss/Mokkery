package dev.mokkery.internal.calls

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.utils.arrayElementType
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.matcher.CompositeVarArgMatcher
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.varargs.VarArgMatcher
import dev.mokkery.matcher.varargs.VarargMatcherMarker
import kotlin.collections.plus

internal interface ArgMatchersComposer {

    fun compose(arg: CallArgument, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?>
}

internal fun ArgMatchersComposer(): ArgMatchersComposer = ArgMatchersComposerImpl()

private class ArgMatchersComposerImpl : ArgMatchersComposer {
    override fun compose(arg: CallArgument, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        return when {
            arg.parameter.isVararg -> composeVarargs(arg, matchers)
            matchers.isEmpty() -> ArgMatcher.Equals(arg.value)
            else -> compose(arg.parameter.name, matchers)
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
                val matcher = stack.removeAt(stack.lastIndex)
                val composeResult =  composite.compose(matcher)
                composite = when {
                    composite !is CompositeVarArgMatcher && matcher is VarArgMatcher -> VarargMatcherMarker(composeResult)
                    else -> composeResult
                }
            }
            composite.assertFilled()
            stack += composite
        }
        return stack.singleOrNull() ?: throw MultipleMatchersForSingleArgException(name, stack)
    }

    private fun composeVarargs(arg: CallArgument, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        val matcher = compose(arg.parameter.name, matchers + CompositeVarArgMatcher(arg.value.arrayElementType()))
        require(matcher is CompositeVarArgMatcher)
        val matchersSize = matcher.matchers.size
        val expectedMatchersSize = (arg.value.asListOrNull()?.size ?: 0)
        if (matchersSize != expectedMatchersSize) throw VarargsAmbiguityDetectedException()
        return matcher
    }
}
