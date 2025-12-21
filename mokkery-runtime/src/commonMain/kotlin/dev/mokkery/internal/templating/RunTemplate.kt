@file:Suppress("unused")

package dev.mokkery.internal.templating

import dev.mokkery.internal.isMock
import dev.mokkery.internal.isNotMock
import dev.mokkery.internal.mokkeryRuntimeError
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

internal suspend fun <R> MokkeryTemplatingScope.runTemplateSuspend(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    templating: (() -> Map<TemplatingParameter, ArgMatcher<Any?>>)? = null,
    original: (suspend () -> R)? = null
): RunTemplateResult<R> {
    if (mock.isNotMock) {
        if (original == null) mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
        return RunTemplateResult.Original(original())
    } else {
        templatingRegistry.register(mock, mockedType, functionName, templating!!())
        return RunTemplateResult.Empty
    }
}

internal fun <R> MokkeryTemplatingScope.runTemplate(
    mock: Any,
    mockedType: KClass<*>,
    functionName: String,
    templating: (() -> Map<TemplatingParameter, ArgMatcher<Any?>>)? = null,
    original: (() -> R)? = null
): RunTemplateResult<R> {
    if (mock.isNotMock) {
        if (original == null) mokkeryRuntimeError("Using matchers with types that are not mocks is illegal!")
        return RunTemplateResult.Original(original())
    } else {
        templatingRegistry.register(mock, mockedType, functionName, templating!!())
        return RunTemplateResult.Empty
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun mockMethodCallResultAccessError(): Nothing {
    mokkeryRuntimeError(
        "The result of a mock method must not be accessed inside `every` or `verify`." +
                " If you're trying to invoke a method with an extension receiver or context parameters," +
                " use the `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` functions instead."
    )
}
