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

    fun ensureBinding(token: Int, obj: Any?)

    fun interceptArg(token: Int, name: String, arg: Any?): Any?

    fun interceptVarargElement(token: Int, arg: Any?, isSpread: Boolean): Any?

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
) : TemplatingScope {

    private var isReleased = false
    private val currentMatchers = mutableListOf<ArgMatcher<Any?>>()
    private var varargGenericMatcherFlag: Boolean = false

    private val tokenToObj = mutableMapOf<Int, Any?>()
    private val tokenToData = mutableMapOf<Int, TemplateData>()
    override val spies = mutableSetOf<MokkerySpyScope>()
    override val templates = mutableListOf<CallTemplate>()

    override fun ensureBinding(token: Int, obj: Any?) {
        if (isReleased) return
        tokenToObj[token] = obj
        val scope = scopeLookup.resolve(obj)
        if (scope is MokkerySpyScope) {
            val templating = scope.interceptor.templating
            tokenToData.getOrPut(token) { TemplateData() }
            when {
                templating.isEnabledWith(this) -> return
                templating.isEnabled -> throw ConcurrentTemplatingException()
                else -> {
                    spies.add(scope)
                    templating.start(this)
                }
            }
        }
    }

    override fun release() {
        isReleased = true
        spies.forEach { it.interceptor.templating.stop() }
        spies.clear()
    }

    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        if (isReleased) return autofillValue(argType)
        if (matcher is VarArgMatcher) {
            varargGenericMatcherFlag = true
        }
        currentMatchers.add(matcher.unsafeCast())
        return autofillValue(argType)
    }

    override fun interceptVarargElement(token: Int, arg: Any?, isSpread: Boolean): Any? {
        if (isReleased) return arg
        if (isTokenDefinitelyNotMocked(token)) return arg
        val data = this.tokenToData.getOrPut(token) { TemplateData() }
        val args = if (isSpread) {
            arg.toListOrNull() ?: error("Expected array for spread operator, but $arg encountered!")
        } else {
            listOf(arg)
        }
        val size = args.size
        val elementMatchersSize = currentMatchers.subListAfter(data.varargsMatchersCount).size
        if (varargGenericMatcherFlag) {
            varargGenericMatcherFlag = false
            if (elementMatchersSize != size + 1) throw VarargsAmbiguityDetectedException()
            data.varargsMatchersCount++
            return arg
        }
        if (elementMatchersSize != 0 && elementMatchersSize < size) throw VarargsAmbiguityDetectedException()
        args.forEachIndexed { index, vararg ->
            val matcher = currentMatchers.getOrNull(data.varargsMatchersCount + index)
            if (matcher == null) currentMatchers.add(ArgMatcher.Equals(vararg))
        }
        data.varargsMatchersCount += size
        return arg
    }

    override fun interceptArg(token: Int, name: String, arg: Any?): Any? {
        if (isReleased) return arg
        if (isTokenDefinitelyNotMocked(token)) return arg
        val data = getDataFor(token)
        data.matchers[name] = currentMatchers.toMutableList()
        currentMatchers.clear()
        return arg
    }

    override fun saveTemplate(receiver: String, name: String, args: List<CallArg>) {
        if (isReleased) return
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, signatureGenerator.generate(name, args), matchers.toMap())
    }

    private fun flush(args: List<CallArg>): List<Pair<String, ArgMatcher<Any?>>> {
        val token = tokenToObj.filterValues { scopeLookup.resolve(it) != null }.keys.first()
        val registeredMatchers = getDataFor(token).matchers
        val matchersSnapshot = registeredMatchers.toMutableMap()
        clearCurrent()
        return args.map {
            val matchers = matchersSnapshot[it.name].orEmpty()
            it.name to composer.compose(it, matchers)
        }
    }

    private fun clearCurrent() {
        tokenToData.clear()
        tokenToObj.clear()
        varargGenericMatcherFlag = false
        currentMatchers.clear()
    }

    private fun getDataFor(token: Int): TemplateData = tokenToData.getOrPut(token) { TemplateData() }

    private fun isTokenDefinitelyNotMocked(token: Int): Boolean {
        val obj = tokenToObj[token]
        return obj != null && scopeLookup.resolve(obj) == null
    }
}

private class TemplateData(
    val matchers: MutableMap<String, List<ArgMatcher<Any?>>> = mutableMapOf(),
    var varargsMatchersCount: Int = 0,
)
