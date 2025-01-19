@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.getIfProvided
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.context.Function
import dev.mokkery.context.FunctionCall
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.answering.autofill.AnyValueProvider
import dev.mokkery.internal.answering.autofill.asAutofillProvided
import dev.mokkery.internal.context.AssociatedFunctions
import dev.mokkery.internal.context.CurrentMokkeryInstance
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.context.CallArgument
import dev.mokkery.context.MokkeryContext
import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.utils.copyWithReplacedKClasses
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal fun createMokkeryBlockingCallScope(
    instance: MokkeryInstance,
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spyDelegate: kotlin.Function<Any?>? = null
) = MokkeryBlockingCallScope(createMokkeryCallContext(instance, name, returnType, args, supers, spyDelegate))

internal fun createMokkerySuspendCallScope(
    instance: MokkeryInstance,
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>> = emptyMap(),
    spyDelegate: kotlin.Function<Any?>? = null
) = MokkerySuspendCallScope(createMokkeryCallContext(instance, name, returnType, args, supers, spyDelegate))

internal val GlobalMokkeryInstanceLookup
    get() = GlobalMokkeryScope.tools.instanceLookup

private fun createMokkeryCallContext(
    instance: MokkeryInstance,
    name: String,
    returnType: KClass<*>,
    args: List<CallArgument>,
    supers: Map<KClass<*>, kotlin.Function<Any?>>,
    spyDelegate: kotlin.Function<Any?>?
): MokkeryContext {
    val safeArgs = args.copyWithReplacedKClasses()
    val call = FunctionCall(
        function = Function(
            name = name,
            parameters = args.map(CallArgument::parameter),
            returnType = returnType.takeIfImplementedOrAny()
        ),
        args = safeArgs
    )
    return GlobalMokkeryScope.mokkeryContext + CurrentMokkeryInstance(instance) + call + AssociatedFunctions(supers, spyDelegate)
}

internal fun generateMockId(typeName: String) = GlobalMokkeryScope
    .tools
    .instanceIdGenerator
    .generate(typeName)

internal fun <T> autofillConstructor(type: KClass<*>): T = autofillConstructorProvider
    .provideValue(type.takeIfImplementedOrAny())
    .unsafeCast()

internal inline fun <reified T> callIgnoringClassCastException(templatingScope: Any?, block: () -> T): T {
    val initialTemplatesCount = templatingScope.templatesCount
    return try {
        block()
    } catch (e: ClassCastException) {
        autofillOrRethrow(T::class, e, initialTemplatesCount, templatingScope)
    }
}

internal fun <T> autofillOrRethrow(cls: KClass<*>, e: ClassCastException, initialTemplatesCount: Int, scope: Any?): T {
    if (initialTemplatesCount == scope.templatesCount) throw e
    return AutofillProvider
        .forInternals
        .provideValue(cls.takeIfImplementedOrAny())
        .unsafeCast()
}

internal val Any?.templatesCount: Int get() = (this as? TemplatingScope)?.templates?.size ?: 0

private val autofillConstructorProvider = AutofillProvider
    .forInternals
    .providingNotNullIfSupported()

private fun AutofillProvider<Any?>.providingNotNullIfSupported(): AutofillProvider<Any?> {
    val notNullProvider = AnyValueProvider.notNullIfSupported() ?: return this
    val original = this
    return object : AutofillProvider<Any> {
        override fun provide(type: KClass<*>): AutofillProvider.Value<Any> {
            val value = original.provide(type).getIfProvided()
                ?: notNullProvider.provide(type).getIfProvided()
                ?: mokkeryRuntimeError("Required instance of type $type, but internal machinery was not able to provide one!")
            return value.asAutofillProvided()
        }
    }
}
