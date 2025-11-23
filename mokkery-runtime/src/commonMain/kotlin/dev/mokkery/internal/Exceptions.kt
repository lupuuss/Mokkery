package dev.mokkery.internal

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.internal.utils.bestName
import kotlin.reflect.KClass

internal class CallNotMockedException(name: String) : MokkeryRuntimeException(message = "Call $name not mocked!")

@PublishedApi
internal class MokkeryIntrinsicException : MokkeryRuntimeException(
    message = "This call should be implemented as intrinsic by the Mokkery compiler plugin!" +
            " Please make sure you applied the plugin correctly!"
)

internal class ObjectNotMockedException(obj: Any?) : MokkeryRuntimeException("$obj is not mocked by Mokkery!")

internal class ObjectIsNotSpyException(obj: Any?) : MokkeryRuntimeException("$obj must be a spy for this operation, but it is a mock!")

internal class ObjectIsNotMockException(obj: Any?) : MokkeryRuntimeException("$obj must be a mock for this operation, but it is a spy!")

internal class NotSingleCallInEveryBlockException : MokkeryRuntimeException("Each 'every' block requires single mock call!")

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

internal class MissingSpyMethodException() : MokkeryRuntimeException("Spied method not found!")

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
