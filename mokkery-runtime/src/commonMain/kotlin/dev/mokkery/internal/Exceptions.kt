package dev.mokkery.internal

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

@PublishedApi
internal val mokkeryIntrinsic: Nothing
    get() = throw MokkeryIntrinsicException()

internal fun mokkeryRuntimeError(message: String): Nothing = throw MokkeryRuntimeException(message)

internal class CallNotMockedException(name: String) : MokkeryRuntimeException(message = "Call $name not mocked!")

@PublishedApi
internal class MokkeryIntrinsicException : MokkeryRuntimeException(
    message = "This call should be implemented as intrinsic by the Mokkery compiler plugin!" +
            " Please make sure you applied the plugin correctly!"
)

internal class ObjectNotMockedException(obj: Any?) : MokkeryRuntimeException("$obj is not mocked by Mokkery!")

internal class ObjectIsNotSpyException(obj: Any?) : MokkeryRuntimeException("$obj must be a spy for this operation, but it is a mock!")

internal class ObjectIsNotMockException(obj: Any?) : MokkeryRuntimeException("$obj must be a mock for this operation, but it is a spy!")

internal class SingleCallInEveryBlockRequiredException(
    templates: List<String>,
) : MokkeryRuntimeException(
    buildString {
        append("Each 'every' block requires exactly one call to a mock,")
        when {
            templates.isEmpty() -> {
                appendLine(" but there are no calls to any mock!")
                appendLine()
                appendLine(noTemplatesCommonReasons)
            }
            else -> {
                appendLine(" but there are more calls than expected:")
                templates.forEachIndexed { index, template ->
                    append(index + 1)
                    append(". ")
                    appendLine(template)
                }
            }
        }
    }
)
internal class SuspiciousEmptyVerifyBlockException : MokkeryRuntimeException(
    "Given 'verify' block does not contain any call to a mock. It's very suspicious and most probably caused by misuse.\n\n$noTemplatesCommonReasons"
)

private val noTemplatesCommonReasons = """
    Possible reasons:
    * You are calling an object that is not a mock.
    * You are calling a mock, but the member function is final.
    * You are calling a mock, but it's an extension function instead of a member function.
""".trimIndent()

internal class SuspendingAnswerBlockingCallException : MokkeryRuntimeException(
    message = "Regular function was mocked with answer that is for suspending functions only!"
)

internal class BlockingAnswerSuspendingCallException : MokkeryRuntimeException(
    message = "Suspend function was mocked with answer that is for blocking functions only!"
)

internal class DefaultNothingException :
    MokkeryRuntimeException("This is the default exception for Nothing return type!")

internal class NoMoreSequentialAnswersException : MokkeryRuntimeException(
    "No more sequentially defined answers!"
)

internal class MissingSuperMethodException(
    types: List<KClass<*>>
) : MokkeryRuntimeException("Super call for ${superTypesString(types)} not found!") {

    constructor(type: KClass<*>) : this(listOf(type))
}

private fun superTypesString(types: List<KClass<*>>): String {
    return when (types.size) {
        1 -> "type '${types.single().bestName()}'"
        else -> "any of types ${types.map(KClass<*>::bestName)}"
    }
}


internal class IncorrectArgsForSuperMethodException(expectedCount: Int, actualCount: Int) : MokkeryRuntimeException(
    "Super call requires $expectedCount arguments but $actualCount provided!"
)

internal class IncorrectArgsForSpiedMethodException(expectedCount: Int, actualCount: Int) : MokkeryRuntimeException(
    "Spied call requires $expectedCount arguments but $actualCount provided!"
)

internal class MissingSpyMethodException : MokkeryRuntimeException("Spied method not found!")

internal class SuperTypeMustBeSpecifiedException(
    reason: String
) : MokkeryRuntimeException("You must specify super type! Reason: $reason")

internal class IllegalSuspensionException : MokkeryRuntimeException(
    "`everySuspend`/`verifySuspend` does not support actual suspension! Only mock method calls are allowed!"
)

internal class AbsentValueInSlotException : MokkeryRuntimeException(
    "Expected value in slot, but it is absent!"
)

internal class MokkerySuiteScopeNotImplementedException : MokkeryRuntimeException(
    """
        This method should be overridden by the Mokkery compiler plugin for any class in your source code.
        If you're seeing this error, it likely means that the Mokkery plugin is either not applied or incorrectly configured.
        Another possible cause is that `MokkerySuiteScope` is inherited indirectly through another interface. In this case, you can either inherit `MokkerySuiteScope` directly or manually implement it by delegating to an instance created with the `MokkerySuiteScope` function.  
    """.trimIndent()
)

internal class MockMemberCallResultAccessException(
    receiver: String,
    functionName: String,
) : MokkeryRuntimeException(
    """
        The result of calling `$functionName` on $receiver must not be accessed inside `every` or `verify`.
        
        If you're trying to mock a member function with an extension receiver or context parameters, use `dev.mokkery.templating.ext` or `dev.mokkery.templating.ctx` instead of Kotlin scope functions (e.g. `let`, `run`). 
        Otherwise, using scope functions here is not supported.
        """.trimIndent()
)

internal class MockFinalMemberCallException(
    receiver: String,
    functionName: String,
) : MokkeryRuntimeException(
    """
        `$functionName` is final and cannot be mocked on $receiver.
        Only non-final member functions can be intercepted inside `every` or `verify`.
        """.trimIndent()
)

internal class MockCallExpectedException(
    mock: Any,
    mockedType: KClass<*>,
    call: String,
) : MokkeryRuntimeException(
    "Call to `$call` was expected to be performed on a mock of ${mockedType.simpleName ?: "anonymous"} type, but the receiver was not a mock - it was an instance of ${mock::class.simpleName ?: "anonymous"} type => $mock"
)
