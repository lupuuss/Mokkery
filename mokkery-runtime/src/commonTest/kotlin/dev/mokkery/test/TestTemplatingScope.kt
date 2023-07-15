package dev.mokkery.test

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal class TestTemplatingScope(
    override val spies: Set<MokkerySpyScope> = emptySet(),
    override val templates: List<CallTemplate> = emptyList(),
) : TemplatingScope {

    private val _recordedSaveCalls = mutableListOf<TemplateParams>()
    val recordedSaveCalls: List<TemplateParams> = _recordedSaveCalls
    var released = false
    val argMatchersScope = TestArgMatchersScope()

    override fun <T> ensureBinding(obj: T): T = pluginMethodError()

    override fun interceptArg(name: String, arg: Any?): Any =  pluginMethodError()

    override fun interceptVarargElement(arg: Any?, isSpread: Boolean): Any = pluginMethodError()

    override fun saveTemplate(receiver: String, name: String, args: List<CallArg>) {
        _recordedSaveCalls.add(TemplateParams(receiver, name, args))
    }

    override fun release() {
        released = true
    }

    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        return argMatchersScope.matches(argType, matcher)
    }

    private fun pluginMethodError(): Nothing =
        error("This call is only for compiler plugin and it should not be called from runtime code!")

    data class TemplateParams(
        val receiver: String, val name: String, val args: List<CallArg>
    )
}
