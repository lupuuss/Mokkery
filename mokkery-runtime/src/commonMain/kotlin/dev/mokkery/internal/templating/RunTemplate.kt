@file:Suppress("unused")

package dev.mokkery.internal.templating

import dev.mokkery.context.Function
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.isMock
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.reflect.KClass

internal sealed interface RunTemplateResult<out T> {

    val value: T

    data class Original<T>(override val value: T): RunTemplateResult<T>

    data object Empty : RunTemplateResult<Nothing> {
        override val value: Nothing
            get() = mockMethodCallResultAccessError()
    }
}

internal fun <T> checkNotMock(obj: T): T {
    if (obj.isMock) mockMethodCallResultAccessError()
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
        RunTemplateResult.Empty
    }
    original == null -> mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
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
        RunTemplateResult.Empty
    }
    original == null -> mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
    else -> RunTemplateResult.Original(original())
}

@Suppress("NOTHING_TO_INLINE")
private inline fun mockMethodCallResultAccessError(): Nothing {
    mokkeryRuntimeError(
        "The result of a mock method must not be accessed inside `every` or `verify`." +
                " If you're trying to invoke a method with an extension receiver or context parameters," +
                " use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead."
    )
}
