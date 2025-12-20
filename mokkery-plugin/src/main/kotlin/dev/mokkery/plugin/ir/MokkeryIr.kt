package dev.mokkery.plugin.ir

import dev.mokkery.plugin.Mokkery.dev_mokkery
import dev.mokkery.plugin.Mokkery.dev_mokkery_annotations
import dev.mokkery.plugin.Mokkery.dev_mokkery_context
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_context
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_defaults
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_matcher
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_templating
import dev.mokkery.plugin.Mokkery.dev_mokkery_internal_utils
import dev.mokkery.plugin.Mokkery.dev_mokkery_matcher
import dev.mokkery.plugin.Mokkery.dev_mokkery_templating
import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

object MokkeryIr {
    object Class {
        val Matcher by dev_mokkery_annotations.klass
        val MockMode by dev_mokkery.klass
        val MokkeryScope by dev_mokkery.klass
        val MokkeryMatcherScope by dev_mokkery_matcher.klass
        val ArgMatcher by dev_mokkery_matcher.klass
        val CompositeVarArgMatcher by dev_mokkery_internal_matcher.klass
        val DefaultValuesMatcher by dev_mokkery_internal_matcher.klass
        val MockMany2 by dev_mokkery.klass
        val MockMany3 by dev_mokkery.klass
        val MockMany4 by dev_mokkery.klass
        val MockMany5 by dev_mokkery.klass

        val MokkerySuiteScope by dev_mokkery.klass

        val MokkeryInstanceScope by dev_mokkery.klass

        val CallArgument by dev_mokkery_context.klass
        val SuiteName by dev_mokkery_internal_context.klass

        val MokkeryTemplatingScope by dev_mokkery_templating.klass
        val TemplatingParameter by dev_mokkery_internal_templating.klass
        val RunTemplateResult by dev_mokkery_internal_templating.klass

        val DefaultsExtractorFactory by dev_mokkery_internal_defaults.klass

        fun mockMany(value: Int): ClassResolver {
            return mockManyMap[value]
                ?: error("Unsupported types number! Expected value: in ${2..5}; Actual value: $value")
        }

        private val mockManyMap = mapOf(
            2 to MockMany2,
            3 to MockMany3,
            4 to MockMany4,
            5 to MockMany5,
        )
    }

    object Function {
        val internalEvery by dev_mokkery_internal.function
        val internalEverySuspend by dev_mokkery_internal.function
        val internalVerify by dev_mokkery_internal.function
        val internalVerifySuspend by dev_mokkery_internal.function
        val runTemplate by dev_mokkery_internal_templating.function
        val runTemplateSuspend by dev_mokkery_internal_templating.function
        val checkNotMock by dev_mokkery_internal_templating.function
        val MokkerySuiteScope by dev_mokkery.function
        val createInstanceScope by dev_mokkery_internal.function
        val createInstanceContext by dev_mokkery_internal.function
        val initializeInJsFunctionMock by dev_mokkery_internal.function
        val typeArgumentAt by dev_mokkery_internal.function
        val invokeInstantiationListener by dev_mokkery_internal_context.function
        val createBlockingCallScope by dev_mokkery_internal.function
        val createSuspendCallScope by dev_mokkery_internal.function
        val inlineLiteralsAsMatchers by dev_mokkery_internal_matcher.function
        val throwArguments by dev_mokkery_internal_defaults.function
        val methodWithoutDefaultsError by dev_mokkery_internal_defaults.function
        val matches by dev_mokkery_matcher.function { it.owner.parameters.size == 2 }
        val matchesComposite by dev_mokkery_matcher.function
        val spread by dev_mokkery_internal_matcher.function
        val mokkeryRuntimeError by dev_mokkery_internal_utils.function
    }

    object Property {

        val instanceIdString by dev_mokkery_internal.property
        val spiedObject by dev_mokkery_internal.property
        val callInterceptor by dev_mokkery_internal_context.property
    }

    val Origin = IrDeclarationOrigin.GeneratedByPlugin(Key)

    object Key : GeneratedDeclarationKey() {
        override fun toString(): String = "MokkeryPlugin"
    }
}

val FqName.klass: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, ClassResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = ClassById(ClassId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.function: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FunctionResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = FunctionById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

val FqName.property: PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, PropertyResolver>>
    get() = PropertyDelegateProvider { _: Any?, property ->
        val resolver = PropertyById(CallableId(this, Name.identifier(property.name)))
        ReadOnlyProperty { _, _ -> resolver }
    }

fun FqName.function(
    predicate: (IrSimpleFunctionSymbol) -> Boolean
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, FunctionResolver>> {
    return PropertyDelegateProvider { _: Any?, property ->
        val resolver = FunctionById(CallableId(this, Name.identifier(property.name)), predicate)
        ReadOnlyProperty { _, _ -> resolver }
    }
}
