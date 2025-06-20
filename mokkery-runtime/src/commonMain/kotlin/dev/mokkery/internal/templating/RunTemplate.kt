package dev.mokkery.internal.templating

import dev.mokkery.internal.mokkeryScope
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal sealed interface TemplateOriginalResult<out T> {

    val value: T

    data class Value<T>(override val value: T): TemplateOriginalResult<T>

    data object Empty : TemplateOriginalResult<Nothing> {
        override val value: Nothing
            get() = mokkeryRuntimeError(
                "Result of mock should not be accessed inside `every` and `verify`! If you are trying to invoke method with extension receiver or context parameters, use `ext` or `ctx` functions!"
            )
    }
}

internal suspend fun <R> TemplatingScope.runTemplateSuspend(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    templating: (() -> Map<TemplatingParameter, ArgMatcher<Any?>>)? = null,
    original: (suspend () -> R)? = null
): TemplateOriginalResult<R> {
    if (mock.mokkeryScope == null) {
        if (original == null) mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
        return TemplateOriginalResult.Value(original())
    } else {
        register(mock, mockedType, functionName, templating!!())
        return TemplateOriginalResult.Empty
    }
}

internal fun <R> TemplatingScope.runTemplate(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    templating: (() -> Map<TemplatingParameter, ArgMatcher<Any?>>)? = null,
    original: (() -> R)? = null
): TemplateOriginalResult<R> {
    if (mock.mokkeryScope == null) {
        if (original == null) mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
        return TemplateOriginalResult.Value(original())
    } else {
        register(mock, mockedType, functionName, templating!!())
        return TemplateOriginalResult.Empty
    }
}
