package dev.mokkery.internal.templating

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.answering.autofillValue
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.matcher.MergedVarArgMatcher
import dev.mokkery.internal.signature.SignatureGenerator
import dev.mokkery.internal.subListAfter
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.varargs.VarArgMatcher
import kotlin.reflect.KClass

internal interface TemplatingScope : ArgMatchersScope {

    val spies: Set<MokkerySpyScope>
    val templates: List<CallTemplate>

    fun <T> ensureBinding(obj: T): T

    fun interceptArg(name: String, arg: Any?): Any?

    fun interceptVarargElement(arg: Any?): Any?

    fun saveTemplate(receiver: String, name: String, args: List<CallArg>)

    fun release()
}

internal fun TemplatingScope(
    signatureGenerator: SignatureGenerator = SignatureGenerator()
): TemplatingScope = TemplatingScopeImpl(signatureGenerator)

private class TemplatingScopeImpl(
    private val signatureGenerator: SignatureGenerator,
): TemplatingScope {

    private val currentMatchers = mutableListOf<ArgMatcher<Any?>?>()
    private val matchers = mutableMapOf<String, List<ArgMatcher<Any?>?>>()
    private var varargGenericMatcherFlag = false
    private var varargsMatchersCount = 0
    override val spies = mutableSetOf<MokkerySpyScope>()
    override val templates = mutableListOf<CallTemplate>()

    override fun <T> ensureBinding(obj: T): T {
        if (obj is MokkerySpyScope) {
            val templating = obj.interceptor.templating
            when {
                templating.isEnabledWith(this) -> return obj
                templating.isEnabled -> throw ConcurrentTemplatingException()
                else -> {
                    spies.add(obj)
                    templating.start(this)
                }
            }
        }
        return obj
    }

    override fun release() {
        spies.forEach { it.interceptor.templating.stop() }
        spies.clear()
    }

    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        if (matcher is VarArgMatcher) {
            varargGenericMatcherFlag = true
        }
        currentMatchers.add(matcher.unsafeCast())
        return autofillValue(argType)
    }

    override fun interceptVarargElement(arg: Any?): Any? {
        val size = arg.toListOrNull()?.size ?: 1
        val elementMatchersSize = currentMatchers.subListAfter(varargsMatchersCount).size
        if (varargGenericMatcherFlag) {
            varargGenericMatcherFlag = false
            if (elementMatchersSize != size + 1) throw VarargsAmbiguityDetectedException()
            varargsMatchersCount++
            return arg
        }
        if (elementMatchersSize != 0 && elementMatchersSize != size) throw VarargsAmbiguityDetectedException()
        for (index in 0 until size) {
            val matcher = currentMatchers.getOrNull(varargsMatchersCount + index)
            if (matcher == null) currentMatchers.add(null)
        }
        varargsMatchersCount += size
        return arg
    }

    override fun interceptArg(name: String, arg: Any?): Any? {
        matchers[name] = currentMatchers.toMutableList()
        currentMatchers.clear()
        return arg
    }

    override fun saveTemplate(receiver: String, name: String, args: List<CallArg>) {
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, signatureGenerator.generate(name, args), matchers.toMap())
    }

    private fun flush(args: List<CallArg>): List<Pair<String, ArgMatcher<Any?>>> {
        val matchersSnapshot = matchers.toMutableMap()
        clearCurrent()
        return args.map {
            val matchers = matchersSnapshot[it.name].orEmpty()
            when {
                !it.isVararg && matchers.size > 1 -> throw MultipleMatchersForSingleArgException(it.name, matchers)
                !it.isVararg -> it.name to (matchers.singleOrNull() ?: ArgMatcher.Equals(it.value))
                else -> it.name to varargMatcher(it, matchers)
            }
        }
    }

    private fun varargMatcher(arg: CallArg, matchers: List<ArgMatcher<Any?>?>): ArgMatcher<Any?> {
        val varArgs = arg.value.toListOrNull() ?: error("Unexpected vararg param!")
        val before = mutableListOf<ArgMatcher<Any?>>()
        var wildcardMatcher: VarArgMatcher? = null
        val after = mutableListOf<ArgMatcher<Any?>>()
        matchers.forEachIndexed { index, matcher ->
            when {
                wildcardMatcher != null && matcher is VarArgMatcher -> throw MultipleVarargGenericMatchersException()
                matcher is VarArgMatcher -> wildcardMatcher = matcher
                wildcardMatcher != null -> after.add(matcher ?: ArgMatcher.Equals(varArgs[index - 1]))
                else -> before.add(matcher ?: ArgMatcher.Equals(varArgs[index]))
            }
        }
        return MergedVarArgMatcher(arg.arrayElementType(), before, wildcardMatcher, after)
    }

    private fun clearCurrent() {
        matchers.clear()
        varargsMatchersCount = 0
        varargGenericMatcherFlag = false
        currentMatchers.clear()
    }

}
