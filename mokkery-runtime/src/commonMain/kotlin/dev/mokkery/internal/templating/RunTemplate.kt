@file:Suppress("unused")

package dev.mokkery.internal.templating

import dev.mokkery.internal.mokkeryScope
import dev.mokkery.internal.utils.bestName
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.templating.TemplatingScope
import kotlin.reflect.KClass

internal sealed interface TemplateOriginalResult<out T> {

    val value: T

    data class Value<T>(override val value: T): TemplateOriginalResult<T>

    data object Empty : TemplateOriginalResult<Nothing> {
        override val value: Nothing
            get() = mokkeryRuntimeError(
                "The result of a mock method should not be accessed inside `every` or `verify`." +
                        " If you're trying to invoke a method with an extension receiver or context parameters," +
                        " use the `dev.mokkery.templating.ext` or `dev.mokkery.matcher.ctx` functions instead."
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

@Suppress("NOTHING_TO_INLINE")
private inline fun TemplatingScope.register(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    input: Map<TemplatingParameter, ArgMatcher<Any?>>
) {
    if (this !is TemplatingScopeImpl) {
        error("Mokkery templating expects `${TemplatingScopeImpl::class.bestName()} but ${this::class.bestName()}` provided!")
    }
    register(mock, mockedType, functionName, input)
}
