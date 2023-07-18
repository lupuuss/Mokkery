package dev.mokkery.internal.templating

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.answering.autofillValue
import dev.mokkery.internal.dynamic.MokkeryScopeLookup
import dev.mokkery.internal.matcher.ArgMatchersComposer
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

    fun interceptVarargElement(arg: Any?, isSpread: Boolean): Any?

    fun saveTemplate(receiver: String, name: String, args: List<CallArg>)

    fun release()
}

internal fun TemplatingScope(
    signatureGenerator: SignatureGenerator = SignatureGenerator(),
    composer: ArgMatchersComposer = ArgMatchersComposer(),
    scopeLookup: MokkeryScopeLookup = MokkeryScopeLookup.current,
): TemplatingScope = TemplatingScopeImpl(signatureGenerator, composer, scopeLookup)

private class TemplatingScopeImpl(
    private val signatureGenerator: SignatureGenerator,
    private val composer: ArgMatchersComposer,
    private val scopeLookup: MokkeryScopeLookup,
): TemplatingScope {

    private val currentMatchers = mutableListOf<ArgMatcher<Any?>>()
    private val matchers = mutableMapOf<String, List<ArgMatcher<Any?>>>()
    private var varargGenericMatcherFlag = false
    private var varargsMatchersCount = 0
    override val spies = mutableSetOf<MokkerySpyScope>()
    override val templates = mutableListOf<CallTemplate>()

    override fun <T> ensureBinding(obj: T): T {
        val scope = scopeLookup.resolve(obj)
        if (scope is MokkerySpyScope) {
            val templating = scope.interceptor.templating
            when {
                templating.isEnabledWith(this) -> return obj
                templating.isEnabled -> throw ConcurrentTemplatingException()
                else -> {
                    spies.add(scope)
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

    override fun interceptVarargElement(arg: Any?, isSpread: Boolean): Any? {
        val args = if (isSpread) {
            arg.toListOrNull() ?: error("Expected array for spread operator, but $arg encountered!")
        } else {
            listOf(arg)
        }
        val size = args.size
        val elementMatchersSize = currentMatchers.subListAfter(varargsMatchersCount).size
        if (varargGenericMatcherFlag) {
            varargGenericMatcherFlag = false
            if (elementMatchersSize != size + 1) throw VarargsAmbiguityDetectedException()
            varargsMatchersCount++
            return arg
        }
        if (elementMatchersSize != 0 && elementMatchersSize < size) throw VarargsAmbiguityDetectedException()
        args.forEachIndexed { index, vararg ->
            val matcher = currentMatchers.getOrNull(varargsMatchersCount + index)
            if (matcher == null) currentMatchers.add(ArgMatcher.Equals(vararg))
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
            it.name to composer.compose(it, matchers)
        }
    }

    private fun clearCurrent() {
        matchers.clear()
        varargsMatchersCount = 0
        varargGenericMatcherFlag = false
        currentMatchers.clear()
    }

}
