package dev.mokkery.internal.templating

import dev.mokkery.context.Function
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.context.mockSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mockId
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.utils.MocksCollection
import dev.mokkery.internal.utils.MutableMocksCollection
import dev.mokkery.internal.utils.generatedCode
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import kotlin.reflect.KClass

internal class TemplatingParameter(
    val name: String,
    val isVararg: Boolean,
    type: KClass<*>? = null,
    val typeArgumentIndex: Int = -1
) {

    val type = type?.takeIfImplementedOrAny()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TemplatingParameter
        if (isVararg != other.isVararg) return false
        if (typeArgumentIndex != other.typeArgumentIndex) return false
        if (name != other.name) return false
        if (type != other.type) return false
        return true
    }

    override fun hashCode(): Int {
        var result = isVararg.hashCode()
        result = 31 * result + typeArgumentIndex
        result = 31 * result + name.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        return result
    }
}

internal class TemplatingScope : ArgMatchersScope {

    private val _templates = mutableListOf<CallTemplate>()
    private val _mocksCollection = MutableMocksCollection()
    val mocks: MocksCollection get() = _mocksCollection
    val templates: List<CallTemplate> get() = _templates

    fun register(
        mock: Any,
        mockedType: KClass<*>,
        functionName: String,
        input: Map<TemplatingParameter, ArgMatcher<Any?>>
    ) {
        val scope = mock.requireInstanceScope()
        _mocksCollection.upsertScope(scope)
        val params = input.keys.map { param ->
            val paramType = param.type
                ?: scope.mockSpec
                    .interceptedTypes
                    .single { it.type == mockedType }
                    .arguments[param.typeArgumentIndex]
            Function.Parameter(
                name = param.name,
                type = paramType.takeIfImplementedOrAny(),
                isVararg = param.isVararg,
            )
        }
        _templates.add(
            CallTemplate(
                mockId = scope.mockId,
                name = functionName,
                signature = scope
                    .tools
                    .signatureGenerator
                    .generate(functionName, params),
                matchers = input.map { (param, matcher) -> param.name to matcher }.toMap(),
            )
        )
    }

    override fun <T> matches(matcher: ArgMatcher<T>): T = generatedCode

    override fun <T> matchesComposite(
        vararg matchers: T,
        builder: (List<ArgMatcher<T>>) -> ArgMatcher.Composite<T>
    ): T = generatedCode


    override fun toString(): String = "ArgMatchersScope(templates=${templates})"
}
