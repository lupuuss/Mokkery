@file:Suppress("unused")

package dev.mokkery.internal.templating

import dev.mokkery.MokkeryScope
import dev.mokkery.context.Function
import dev.mokkery.internal.MockCallExpectedException
import dev.mokkery.internal.MockFinalMemberCallException
import dev.mokkery.internal.MockMemberCallResultAccessException
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.isMock
import dev.mokkery.internal.render.functionName
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.shortInstanceIdString
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.reflect.KClass

internal sealed interface RunTemplateResult<out T> {

    val value: T

    data class Original<T>(override val value: T) : RunTemplateResult<T>

    data class Empty(val obj: Any, val functionName: String) : RunTemplateResult<Nothing> {
        override val value: Nothing
            get() = throw MockMemberCallResultAccessException(
                receiver = obj.requireInstanceScope().shortInstanceIdString,
                functionName = functionName.renderFunctionName(MokkeryScope.global),
            )
    }
}

internal fun <T : Any> checkMockMemberCallResultAccess(obj: T, functionName: String): T {
    if (obj.isMock) throw MockMemberCallResultAccessException(
        receiver = obj.requireInstanceScope().shortInstanceIdString,
        functionName = functionName.renderFunctionName(MokkeryScope.global),
    )
    return obj
}

internal fun <T : Any> checkMockFinalMemberCall(obj: T, functionName: String): T {
    if (obj.isMock) throw MockFinalMemberCallException(
        receiver = obj.requireInstanceScope().shortInstanceIdString,
        functionName = functionName.renderFunctionName(MokkeryScope.global),
    )
    return obj
}

internal fun templatingFunctionParameter(
    mock: Any,
    mockedType: KClass<*>,
    name: String,
    isVararg: Boolean,
    type: KClass<*>? = null,
    typeArgumentIndex: Int = -1,
): Function.Parameter = Function.Parameter(
    name = name,
    isVararg = isVararg,
    type = type?.takeIfImplementedOrAny() ?: mock.requireInstanceScope()
        .instanceSpec
        .interceptedTypes
        .single { it.type == mockedType }
        .arguments[typeArgumentIndex]
)

internal suspend fun <R> MokkeryTemplatingScope.runTemplateSuspend(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    arguments: ((Any, KClass<*>) -> List<Pair<Function.Parameter, ArgMatcher<Any?>>>)? = null,
    original: (suspend () -> R)? = null
): RunTemplateResult<R> = when {
    mock.isMock -> {
        templatingRegistry.register(mock, functionName, arguments?.invoke(mock, mockedType).orEmpty())
        RunTemplateResult.Empty(mock, functionName)
    }
    original == null -> mockCallExpectedError(mock, mockedType, functionName)
    else -> RunTemplateResult.Original(original())
}

internal fun <R> MokkeryTemplatingScope.runTemplate(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    arguments: ((Any, KClass<*>) -> List<Pair<Function.Parameter, ArgMatcher<Any?>>>)? = null,
    original: (() -> R)? = null
): RunTemplateResult<R> = when {
    mock.isMock -> {
        templatingRegistry.register(mock, functionName, arguments?.invoke(mock, mockedType).orEmpty())
        RunTemplateResult.Empty(mock, functionName)
    }
    original == null -> mockCallExpectedError(mock, mockedType, functionName)
    else -> RunTemplateResult.Original(original())
}

private fun mockCallExpectedError(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
): Nothing = throw MockCallExpectedException(
    mock = mock,
    mockedType = mockedType,
    call = functionName
)

private fun String.renderFunctionName(scope: MokkeryScope): String {
    val renderer = scope.tools.renderers.functionName()
    return renderer.render(this)
}
