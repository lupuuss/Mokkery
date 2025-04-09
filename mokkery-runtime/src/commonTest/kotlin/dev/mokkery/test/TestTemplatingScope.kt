package dev.mokkery.test

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MockId
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.utils.MocksCollection
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal class TestTemplatingScope(
    override val mocks: MocksCollection = MocksCollection(),
    override val templates: List<CallTemplate> = emptyList(),
) : TemplatingScope {

    private val _recordedSaveCalls = mutableListOf<TemplateParams>()

    val recordedSaveCalls: List<TemplateParams> = _recordedSaveCalls
    var released = false
    val argMatchersScope = TestArgMatchersScope()

    override var currentGenericReturnTypeHint: KClass<*>? = null

    override fun ensureBinding(token: Int, obj: Any?, genericReturnTypeHint: KClass<*>?) = pluginMethodError()

    override fun <T> interceptArg(token: Int, name: String, arg: T): T = pluginMethodError()

    override fun <T> interceptVarargElement(token: Int, arg: T, isSpread: Boolean): T = pluginMethodError()

    override fun saveTemplate(id: MockId, name: String, args: List<CallArgument>) {
        _recordedSaveCalls.add(TemplateParams(id, name, args))
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
        val id: MockId, val name: String, val args: List<CallArgument>
    )
}
