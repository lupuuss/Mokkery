package dev.mokkery.internal

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.internal.utils.bestName
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal class CallNotMockedException(name: String) : MokkeryRuntimeException(message = "Call $name not mocked!")

@PublishedApi
internal class MokkeryPluginNotAppliedException : MokkeryRuntimeException(
    message = "This call should be replaced by the Mokkery plugin! Please make sure you applied the plugin correctly!"
)

internal class ObjectNotMockedException(obj: Any?) : MokkeryRuntimeException("$obj is not mocked by Mokkery!")

internal class NotSingleCallInEveryBlockException :
    MokkeryRuntimeException("Each 'every' block requires single mock call!")

internal class SuspendingFunctionBlockingCallException : MokkeryRuntimeException(
    message = "Regular function was mocked with suspending call!"
)

internal class ConcurrentTemplatingException : MokkeryRuntimeException(
    "Any concurrent calls involving verify and every are illegal!"
)

internal class DefaultNothingException :
    MokkeryRuntimeException("This is the default exception for Nothing return type!")

internal class MultipleVarargGenericMatchersException :
    MokkeryRuntimeException("Using more than one generic vararg matcher is illegal!")

internal class MultipleMatchersForSingleArgException(name: String, matchers: List<ArgMatcher<Any?>>) :
    MokkeryRuntimeException(
        "Multiple matchers for param '$name' = $matchers"
    )

internal class VarargsAmbiguityDetectedException : MokkeryRuntimeException(
    "Varargs matchers registered in a ambiguous way. Pleas read the documentation how to avoid varargs ambiguity or report an issue."
)

internal class NoMoreSequentialAnswersException : MokkeryRuntimeException(
    "No more sequentially defined answers!"
)

internal class MissingMatchersForComposite(
    compositeName: String,
    expected: Int,
    matchers: List<ArgMatcher<*>>
) : MokkeryRuntimeException(
    "`$compositeName` expects $expected matchers, but received ${matchers.size}! You probably used literal in composite matcher, which is illegal! Received matchers: $matchers"
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


internal class MissingArgsForSuperMethodException(expectedCount: Int, actualCount: Int) : MokkeryRuntimeException(
    "Super call requires $expectedCount arguments but $actualCount provided!"
)

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
