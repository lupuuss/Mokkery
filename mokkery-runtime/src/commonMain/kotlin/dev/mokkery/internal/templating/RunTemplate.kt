@file:Suppress("unused")

package dev.mokkery.internal.templating

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.Function
import dev.mokkery.internal.FunctionCannotBeMockedException
import dev.mokkery.internal.MockCallExpectedException
import dev.mokkery.internal.MockFinalMemberCallException
import dev.mokkery.internal.MockMemberCallResultAccessException
import dev.mokkery.internal.context.functions
import dev.mokkery.internal.isMock
import dev.mokkery.internal.rendering.descriptor.FunctionRenderDescriptor
import dev.mokkery.internal.rendering.functionRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.shortInstanceIdString
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.reflect.KClass

@PublishedApi
internal sealed interface RunTemplateResult<out T> {

    val value: T

    data class Original<T>(override val value: T) : RunTemplateResult<T>

    data class Empty(val obj: Any, val rawFunctionName: String) : RunTemplateResult<Nothing> {
        override val value: Nothing
            get() {
                val scope = obj.requireInstanceScope()
                throw MockMemberCallResultAccessException(
                    receiver = scope.shortInstanceIdString,
                    functionName = rawFunctionName.renderRawFunctionName(scope),
                )
            }
    }
}

@PublishedApi
internal fun <T : Any> checkMockMemberCallResultAccess(obj: T, rawFunctionName: String): T {
    if (obj.isMock) {
        val scope = obj.requireInstanceScope()
        throw MockMemberCallResultAccessException(
            receiver = scope.shortInstanceIdString,
            functionName = rawFunctionName.renderRawFunctionName(scope),
        )
    }
    return obj
}

@PublishedApi
internal fun <T : Any> checkMockFinalMemberCall(obj: T, functionName: String): T {
    if (obj.isMock) {
        val scope = obj.requireInstanceScope()
        throw MockFinalMemberCallException(
            receiver = scope.shortInstanceIdString,
            functionName = functionName.renderRawFunctionName(scope),
        )
    }
    return obj
}

@PublishedApi
internal suspend fun <R> MokkeryTemplatingScope.runTemplateSuspend(
    mock: Any,
    mockedType: KClass<*>,
    functionId: Long,
    functionName: String,
    matchers: (() -> List<ArgMatcher<Any?>>)? = null,
    original: (suspend () -> R)? = null
): RunTemplateResult<R> = when {
    mock.isMock -> registerTemplate(mock, functionId, functionName, matchers)
    original == null -> mockCallExpectedError(mock, mockedType, functionName)
    else -> RunTemplateResult.Original(original())
}

@PublishedApi
internal fun <R> MokkeryTemplatingScope.runTemplate(
    mock: Any,
    mockedType: KClass<*>,
    functionId: Long,
    functionName: String,
    matchers: (() -> List<ArgMatcher<Any?>>)? = null,
    original: (() -> R)? = null
): RunTemplateResult<R> = when {
    mock.isMock -> registerTemplate(mock, functionId, functionName, matchers)
    original == null -> mockCallExpectedError(mock, mockedType, functionName)
    else -> RunTemplateResult.Original(original())
}

private fun MokkeryTemplatingScope.registerTemplate(
    mock: Any,
    functionId: Long,
    functionName: String,
    matchers: (() -> List<ArgMatcher<Any?>>)? = null,
): RunTemplateResult.Empty {
    val matchers = matchers?.invoke().orEmpty()
    val scope = mock.requireInstanceScope()
    val functions = scope.functions
    val id = functions.normalizeId(Function.Id(functionId))
    functions
        .getOrNull(id)
        ?: throw FunctionCannotBeMockedException(functionName = functionName.renderRawFunctionName(scope))
    templatingRegistry.register(scope, id, matchers)
    return RunTemplateResult.Empty(mock, functionName)
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

private fun String.renderRawFunctionName(scope: MokkeryInstanceScope): String {
    val descriptor = FunctionRenderDescriptor.parse(this)
    return scope.withRenderingScope { functionRenderer.render(descriptor) }
}
