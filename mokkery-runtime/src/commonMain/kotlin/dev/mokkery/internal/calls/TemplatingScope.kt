package dev.mokkery.internal.calls

import dev.mokkery.MokkeryScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.ConcurrentTemplatingException
import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.VarargsAmbiguityDetectedException
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mokkeryMockInterceptor
import dev.mokkery.internal.names.SignatureGenerator
import dev.mokkery.internal.utils.MocksContainer
import dev.mokkery.internal.utils.MutableMocksContainer
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.clear
import dev.mokkery.internal.utils.forEach
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.internal.utils.subListAfter
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.internal.utils.upsert
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import kotlin.reflect.KClass

internal interface TemplatingScope : ArgMatchersScope {

    val mocks: MocksContainer
    val templates: List<CallTemplate>
    val currentGenericReturnTypeHint: KClass<*>?

    fun ensureBinding(token: Int, obj: Any?, genericReturnTypeHint: KClass<*>? = null)

    fun <T> interceptArg(token: Int, name: String, arg: T): T

    fun <T> interceptVarargElement(token: Int, arg: T, isSpread: Boolean): T

    fun saveTemplate(receiver: String, name: String, args: List<CallArgument>)

    fun release()
}

internal fun TemplatingScope(scope: MokkeryScope = GlobalMokkeryScope): TemplatingScope = TemplatingScopeImpl(
    signatureGenerator = scope.tools.signatureGenerator,
    composer = scope.tools.argMatchersComposer,
    binder = TemplatingScopeDataBinder(scope.tools.instanceLookup),
    autofill = scope.tools.autofillProvider
)

private class TemplatingScopeImpl(
    private val signatureGenerator: SignatureGenerator,
    private val composer: ArgMatchersComposer,
    private val binder: TemplatingScopeDataBinder,
    private val autofill: AutofillProvider<Any?>
) : TemplatingScope {
    private var isReleased = false
    private val currentArgMatchers = mutableListOf<ArgMatcher<Any?>>()

    override val mocks = MutableMocksContainer()
    override val templates = mutableListOf<CallTemplate>()
    override val currentGenericReturnTypeHint: KClass<*>?
        get() = binder.firstProperlyBoundedData().genericReturnTypeHint

    override fun ensureBinding(token: Int, obj: Any?, genericReturnTypeHint: KClass<*>?) {
        if (isReleased) return
        val scope = binder.bind(token, obj) ?: return
        // filters out unimplemented KClasses on K/N
        binder.getDataFor(token)?.genericReturnTypeHint = genericReturnTypeHint?.takeIfImplementedOrAny()
        val templating = scope.mokkeryMockInterceptor.templating
        when {
            templating.isEnabledWith(this) -> return
            templating.isEnabled -> throw ConcurrentTemplatingException()
            else -> {
                mocks.upsert(scope)
                templating.start(this)
            }
        }
    }

    override fun release() {
        isReleased = true
        mocks.forEach { it.mokkeryMockInterceptor.templating.stop() }
        mocks.clear()
    }

    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        // filters out unimplemented KClasses on K/N
        val safeKClass = argType.takeIfImplementedOrAny()
        if (isReleased) return autofill.provideValue(safeKClass).unsafeCast()
        currentArgMatchers.add(matcher.unsafeCast())
        return autofill.provideValue(safeKClass).unsafeCast()
    }

    override fun <T> interceptVarargElement(token: Int, arg: T, isSpread: Boolean): T {
        if (isReleased) return arg
        val data = binder.getDataFor(token) ?: return arg
        val args = when {
            isSpread -> arg.asListOrNull() ?: mokkeryRuntimeError("Expected array, but $arg encountered!")
            else -> listOf(arg)
        }
        val size = args.size
        val elementMatchersSize = currentArgMatchers.subListAfter(data.varargMatchersCount).size
        if (elementMatchersSize != 0 && elementMatchersSize < size) throw VarargsAmbiguityDetectedException()
        args.forEachIndexed { index, vararg ->
            currentArgMatchers.getOrNull(data.varargMatchersCount + index)
                ?: currentArgMatchers.add(ArgMatcher.Equals(vararg))
        }
        data.varargMatchersCount += elementMatchersSize
        return arg
    }

    override fun <T> interceptArg(token: Int, name: String, arg: T): T {
        if (isReleased) return arg
        val data = binder.getDataFor(token) ?: return arg
        data.matchers[name] = currentArgMatchers.toMutableList()
        currentArgMatchers.clear()
        return arg
    }

    override fun saveTemplate(receiver: String, name: String, args: List<CallArgument>) {
        if (isReleased) return
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, signatureGenerator.generate(name, args), matchers.toMap())
    }

    private fun flush(args: List<CallArgument>): List<Pair<String, ArgMatcher<Any?>>> {
        val namedMatchers = binder.firstProperlyBoundedData()
            .matchers
            .toMutableMap()
        currentArgMatchers.clear()
        binder.reset()
        return args.map {
            val param = it.parameter
            val matchers = namedMatchers[param.name].orEmpty()
            param.name to composer.compose(it, matchers)
        }
    }
}
